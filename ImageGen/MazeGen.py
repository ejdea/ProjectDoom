# Author: Martin Edmunds
# Date: 06/29/2020
# Version 1.1

from cell import *
from tkinter import *
from PIL import Image, ImageDraw
import random
import sys


class MazeGen:
    # Color Enums
    white = 255
    black = 0

    # Program Run Modes
    image_mode = "image"
    canvas_mode = "canvas"
    both_mode = "both"

    def __init__(self, nodes_x, nodes_y, file_name, mode="image", gen_random=False):
        # window spacing
        self.width = 800
        self.height = 800
        self.render_width = 1000
        self.render_height = 1000
        self.mode = mode

        if self.mode == MazeGen.canvas_mode or self.mode == MazeGen.both_mode:
            self.root = Tk()
            # create drawing canvas
            self.canvas = Canvas(self.root, width=self.render_width, height=self.render_height)
            self.canvas.configure(bg="white")
            self.canvas.pack()
        else:
            self.canvas = None

        self.file_name = file_name

        # debug info
        self.pixel_center = 5

        # set number of cells here
        if gen_random:
            _cells = random.randint(3, 20)
            self.num_cells_x = _cells
            self.num_cells_y = _cells
        else:
            self.num_cells_x = nodes_x
            self.num_cells_y = nodes_y

        # number of pixels to offset in x,y direction to center the screen
        self.pixel_offset_x = (self.width / (self.num_cells_x + 1)) + ((self.render_width - self.width) / 2)
        self.pixel_offset_y = (self.height / (self.num_cells_y + 1)) + ((self.render_height - self.height) / 2)

        # wall length calculations to draw the correct length depending on the number of nodes
        self.wall_length_x = (self.width - self.pixel_offset_x) / self.num_cells_x
        self.wall_length_y = (self.height - self.pixel_offset_y) / self.num_cells_y
        self.wall_length_x /= 2
        self.wall_length_y /= 2

        # create image and drawing tool
        if self.mode == MazeGen.image_mode or self.mode == MazeGen.both_mode:
            self.image = Image.new("L", (self.render_width, self.render_height), MazeGen.white)
            self.draw = ImageDraw.Draw(self.image)
        else:
            self.image = None
            self.draw = None

        # build graph information
        self.cells = []
        self.build_cells(self.num_cells_x, self.num_cells_y)
        self.draw_cells()
        for i in range(len(self.cells)):
            self.build_neighbors(i)

        # self.cells[0].remove_wall(self.canvas, Cell.right)
        self.gen_maze_depth_first()

        # randomly select two cells on the edges to clear
        self.make_exits()

        # save image
        if self.mode != MazeGen.canvas_mode:
            self.to_image()

    def build_cells(self, x: int, y: int):
        step_x = 1 / self.num_cells_x
        step_y = 1 / self.num_cells_y
        # set cell build mode
        if self.mode == MazeGen.image_mode:
            mode = Cell.image_mode
        elif self.mode == MazeGen.canvas_mode:
            mode = Cell.canvas_mode
        else:
            mode = Cell.both_mode

        for i in range(x):
            for j in range(y):
                self.cells.append(Cell(i * step_x, j * step_y, self, self.canvas, self.draw, mode))

    def build_neighbors(self, index):
        # build neighbors for each node in the self.cells list
        # bottom neighbor = index += 1
        # deconstruct index
        _x = index // self.num_cells_x
        _y = index % self.num_cells_y
        bot_neighbor = (_x, _y - 1)
        top_neighbor = (_x, _y + 1)
        right_neighbor = (_x + 1, _y)
        left_neighbor = (_x - 1, _y)

        if self.in_bounds(bot_neighbor[0], bot_neighbor[1]):
            neighbor_index = bot_neighbor[0] * self.num_cells_x + bot_neighbor[1]
            self.cells[index].add_neighbor(self.cells[neighbor_index])
        if self.in_bounds(top_neighbor[0], top_neighbor[1]):
            neighbor_index = top_neighbor[0] * self.num_cells_x + top_neighbor[1]
            self.cells[index].add_neighbor(self.cells[neighbor_index])
        if self.in_bounds(right_neighbor[0], right_neighbor[1]):
            neighbor_index = right_neighbor[0] * self.num_cells_x + right_neighbor[1]
            self.cells[index].add_neighbor(self.cells[neighbor_index])
        if self.in_bounds(left_neighbor[0], left_neighbor[1]):
            neighbor_index = left_neighbor[0] * self.num_cells_x + left_neighbor[1]
            self.cells[index].add_neighbor(self.cells[neighbor_index])

    def gen_maze_depth_first(self):
        stack = []
        # pick random cell
        current = random.choice(self.cells)
        current.visited = True
        stack.append(current)
        done = False
        while not done:
            # select random neighbor cell that haven't been visited
            if len(stack) == 0:
                break
            current = stack.pop(len(stack) - 1)
            candidates = []
            for node in current.neighbor_lookup.values():
                if not node.visited:
                    candidates.append(node)
            # select random neighbor
            if len(candidates) == 0:
                # backtrack
                if len(stack) != 0:
                    current = stack.pop(len(stack) - 1)
                else:
                    done = True
            else:
                while len(candidates) != 0:
                    next = random.choice(candidates)
                    candidates.remove(next)
                    current.remove_wall_node(next)
                    next.visited = True
                    stack.append(next)
        return

    def in_bounds(self, x, y):
        if 0 <= x < self.num_cells_x:
            if 0 <= y < self.num_cells_y:
                return True
        return False

    def draw_cells(self):
        for cell in self.cells:
            cell.draw_cell()

    def make_exits(self):
        # select two nodes along the edges of the self.cells container
        # node is on an edge if (x == 0 || x == self.num_cells_x - 1) || (y == 0 || y == self.num_cells_y - 1)

        # pick if x or y will be the edge
        x_edge_choices = [True, False]
        x_choice = random.choice(x_edge_choices)

        # if x is edge, select possible edge value
        if x_choice:
            y_choice = random.randint(0, self.num_cells_y - 1)
            x_choice = random.choice([0, self.num_cells_x - 1])
        else:
            x_choice = random.randint(0, self.num_cells_x - 1)
            y_choice = random.choice([0, self.num_cells_y - 1])

        # get edge node
        exit_node = self.cells[(x_choice * self.num_cells_x) + y_choice]
        x_edge = (x_choice == 0 or x_choice == self.num_cells_x - 1)
        if x_edge:
            if x_choice == 0:
                # left edge, delete left wall
                exit_node_delete_dir = Cell.left
            else:
                # right edge, delete right wall
                exit_node_delete_dir = Cell.right
        else:
            # y was the edge
            if y_choice == 0:
                # top edge, delete top wall
                exit_node_delete_dir = Cell.top
            else:
                exit_node_delete_dir = Cell.bottom

        x_edge_choices = [True, False]
        x_choice = random.choice(x_edge_choices)

        # if x is edge, select possible edge value
        if x_choice:
            y_choice = random.randint(0, self.num_cells_y - 1)
            x_choice = random.choice([0, self.num_cells_x - 1])
        else:
            x_choice = random.randint(0, self.num_cells_x - 1)
            y_choice = random.choice([0, self.num_cells_y - 1])

        # get second edge node
        start_node = self.cells[(x_choice * self.num_cells_x) + y_choice]
        x_edge = (x_choice == 0 or x_choice == self.num_cells_x - 1)
        if x_edge:
            if x_choice == 0:
                # left edge, delete left wall
                start_node_delete_dir = Cell.left
            else:
                # right edge, delete right wall
                start_node_delete_dir = Cell.right
        else:
            # y was the edge
            if y_choice == 0:
                # top edge, delete top wall
                start_node_delete_dir = Cell.top
            else:
                start_node_delete_dir = Cell.bottom

        exit_node.remove_wall_dir(exit_node_delete_dir)
        start_node.remove_wall_dir(start_node_delete_dir)
        return

    def run(self):
        self.root.mainloop()

    def world_to_screen(self, x: float, y: float) -> (int, int):
        # converts a 0 - 1 value to a pixel coordinate value in screen space
        screen_x = ((x * (self.width - self.pixel_offset_x)) + self.pixel_offset_x)
        screen_y = ((y * (self.height - self.pixel_offset_y)) + self.pixel_offset_y)
        return screen_x, screen_y

    def to_image(self):
        self.image.save(self.file_name)
        return

    def __del__(self):
        if self.mode == MazeGen.canvas_mode or self.mode == MazeGen.both_mode:
            self.root.destroy()


if __name__ == '__main__':
    """
    Expected usage = python3 MazeGen.py [nodes_x] [nodes_y] [mode] [count] [-random]
    """
    debug_flag = False
    default_picture_type = ".png"

    if not debug_flag:
        default_file_name_base = "maze"
        num_pictures = 1
        node_x = 0
        node_y = 0

        # [0] = MazeGen.py, [1] = nodes_x, [2] - nodes_y , [3] = image mode, [4] = count, [5] = -r optional random flag
        if len(sys.argv) < 4:
            print("Expected at least 3 arguments")
            exit(1)

        try:
            node_x = int(sys.argv[1])
            node_y = int(sys.argv[2])
        except:
            print("Incorrect Usage")
            exit(1)

        mode = sys.argv[3]
        mode = mode[1:]

        # -image flag
        if mode == MazeGen.image_mode:
            if len(sys.argv) < 5:
                print("Expected at least 4 arguments with 'image' mode")
                exit(1)
            try:
                num_pictures = int(sys.argv[4])
            except:
                print("Incorrect Usage")
                exit(1)
        # -both flag
        elif mode == MazeGen.both_mode:
            if len(sys.argv) < 5:
                print("Expected at least 5 arguments with 'both' mode")
                exit(1)
            try:
                num_pictures = int(sys.argv[4])
            except:
                print("Incorrect Usage")
                exit(1)
        else:
            if mode != MazeGen.canvas_mode:
                print("Incorrect Usage")
                exit(1)

        # check for random flag
        random_flag = False
        for arg in sys.argv:
            if arg == "-r":
                random_flag = True

        mode = sys.argv[3]
        mode = mode[1:]
        if mode == MazeGen.image_mode:
            for i in range(num_pictures):
                file_name = default_file_name_base + str(i) + default_picture_type
                maze = MazeGen(node_x, node_y, file_name, mode, random_flag)
        elif mode == MazeGen.canvas_mode:
            for i in range(num_pictures):
                file_name = default_file_name_base + str(i) + default_picture_type
                maze = MazeGen(node_x, node_y, file_name, mode, random_flag)
                maze.run()
        else:
            for i in range(num_pictures):
                file_name = default_file_name_base + str(i) + default_picture_type
                maze = MazeGen(node_x, node_y, file_name, mode, random_flag)
                maze.run()
    else:
    # test configuration
        mode = "image"
        gen = MazeGen(20, 20, "test" + default_picture_type, mode)
        if mode != MazeGen.image_mode:
            gen.run()
