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
        self.mode = mode
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

        # window spacing
        self.width = 800
        self.height = 800
        self.render_width = 1000
        self.render_height = 1000
        # set number of pictures to offset the start of the grid
        self.pixel_offset_x = (3 * (self.render_width - self.width)) / 4
        self.pixel_offset_y = (3 * (self.render_height - self.width)) / 4
        # prevent odd clipping behavior if allowed to round
        self.pixel_offset_x = int(self.pixel_offset_x) - 1
        self.pixel_offset_y = int(self.pixel_offset_y) - 1
        # wall length calculations to draw the correct length depending on the number of nodes
        self.wall_length_x = (self.width - self.pixel_offset_x) / self.num_cells_x
        self.wall_length_y = (self.height - self.pixel_offset_y) / self.num_cells_y
        self.wall_length_x /= 2
        self.wall_length_y /= 2

        if self.mode == MazeGen.canvas_mode or self.mode == MazeGen.both_mode:
            self.root = Tk()
            # create drawing canvas
            self.canvas = Canvas(self.root, width=self.render_width, height=self.render_height)
            self.canvas.configure(bg="white")
            self.canvas.pack()
        else:
            self.canvas = None

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

        # SELECT MAZE GEN ALGORITHM
        # self.gen_maze_depth_first()
        # self.gen_maze_kruskal()
        self.gen_maze_prim()

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

        counter = 0
        for i in range(x):
            for j in range(y):
                self.cells.append(Cell(counter, i * step_x, j * step_y, self, self.canvas, self.draw, mode))
                counter += 1

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

    def gen_maze_kruskal(self):
        # create list of edges
        walls = []
        nodes = []
        for i in range(len(self.cells)):
            # append (node, walls)
            nodes.append((self.cells[i]))

        while len(nodes) > 0:
            print(len(nodes))
            # select random node from wall
            node = random.choice(nodes)

            # select random wall from node
            walls = node.get_walls()
            if len(walls) == 0:
                nodes.remove(node)
                continue
            wall = random.choice(node.get_walls())
            node.wall_state[wall] = False
            # check if cells are in distinct sets
            neighbor = node.get_neighbor(wall)
            if neighbor is not None:
                # neighbor.wall_state[wall] = False

                # check if cells are in distinct set
                if len(neighbor.set.intersection(node.set)) == 0:
                    node.remove_wall_node(neighbor)
                    node.set = node.set.union(neighbor.set)
                    neighbor.set = neighbor.set.union(node.set)
            else:
                node.wall_state[wall] = False

    def gen_maze_prim(self):
        # grid of walls
        # list of walls
        walls = {}
        cells = []
        for i in range(len(self.cells)):
            # append (node, walls)
            cells.append((self.cells[i]))
        # pick a cell
        cell = random.choice(cells)
        cell.visited = True
        walls[cell.index] = cell.get_walls()

        while len(walls) > 0:
            # pick random wall from the wall_list
            node = random.choice(list(walls.keys()))
            node = self.cells[node]
            if len(node.get_walls()) == 0:
                del walls[node.index]
                continue
            wall = random.choice(node.get_walls())
            # get cell that divides wall
            neighbor = node.get_neighbor(wall)
            if neighbor is None:
                # mark walls as visited
                node.wall_state[wall] = False
                walls[node.index] = node.get_walls()
                # update the walls
                if len(node.get_walls()) == 0:
                    # remove node from the dict
                    del walls[node.index]
            else:
                if node.visited ^ neighbor.visited:
                    unvisited_node = None
                    if not node.visited:
                        unvisited_node = node
                    else:
                        unvisited_node = neighbor

                    node.remove_wall_node(neighbor)

                    unvisited_node.visited = True
                    # add unvisited nodes walls to the wall list
                    walls[neighbor.index] = neighbor.get_walls()
                    walls[node.index] = node.get_walls()
                    # remove the wall from the list
                else:
                    node.wall_state[wall] = False
                if len(node.get_walls()) == 0:
                    # remove node from the dict
                    if walls.get(node.index) is not None:
                        del walls[node.index]
                if len(neighbor.get_walls()) == 0:
                    # remove node from the dict
                    if walls.get(neighbor.index) is not None:
                        del walls[neighbor.index]
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
        # 0,0 maps to 100, 100
        # 1,1 maps to 900, 900

        screen_x = ((x * (self.width - self.pixel_offset_x)) + self.pixel_offset_x + self.wall_length_x)
        screen_y = ((y * (self.height - self.pixel_offset_y)) + self.pixel_offset_y + self.wall_length_y)
        return screen_x, screen_y

    def to_image(self):
        self.image.save(self.file_name)
        return

    def update(self):
        self.root.update()
        self.root.update_idletasks()

    def __del__(self):
        if self.mode == MazeGen.canvas_mode or self.mode == MazeGen.both_mode:
            self.root.destroy()


if __name__ == '__main__':
    """
    Expected usage = python3 MazeGen.py [nodes_x] [nodes_y] [mode] [count] [-random]
    """
    debug_flag = True
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
        mode = "both"
        gen = MazeGen(15, 15, "test" + default_picture_type, mode)
        if mode != MazeGen.image_mode:
            gen.run()
