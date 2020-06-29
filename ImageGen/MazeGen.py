# Author: Martin Edmunds
# Date: 06/29/2020
# TODO: add comments

from cell import *
from tkinter import *
from PIL import ImageGrab
import random
import time
import sys


class MazeGen:
    def __init__(self, file_name, nodes, gen_random=False):
        self.root = Tk()
        self.file_name = file_name

        # window spacing
        self.width = 800
        self.height = 800
        self.render_width = 1000
        self.render_height = 1000

        # debug info
        self.pixel_center = 5

        # set number of cells here
        if gen_random:
            _cells = random.randint(3, 20)
            self.num_cells_x = _cells
            self.num_cells_y = _cells
        else:
            self.num_cells_x = nodes
            self.num_cells_y = nodes

        # number of pixels to offset in x,y direction to center the screen
        self.pixel_offset_x = (self.width / (self.num_cells_x + 1)) + ((self.render_width - self.width) / 2)
        self.pixel_offset_y = (self.height / (self.num_cells_y + 1)) + ((self.render_height - self.height) / 2)

        # wall length calculations to draw the correct length depending on the number of nodes
        self.wall_length_x = (self.width - self.pixel_offset_x) / self.num_cells_x
        self.wall_length_y = (self.height - self.pixel_offset_y) / self.num_cells_y
        self.wall_length_x /= 2
        self.wall_length_y /= 2

        # create drawing canvas
        self.canvas = Canvas(self.root, width=self.render_width, height=self.render_height)
        self.canvas.configure(bg="white")
        self.canvas.pack()

        # graph information
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
        self.to_image()

    def build_cells(self, x: int, y: int):
        step_x = 1 / self.num_cells_x
        step_y = 1 / self.num_cells_y
        for i in range(x):
            for j in range(y):
                self.cells.append(Cell(i * step_x, j * step_y))

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
                    current.remove_wall(self.canvas, next)
                    next.visited = True
                    stack.append(next)
        return

    def in_bounds(self, x, y):
        if x >= 0 and x < self.num_cells_x:
            if y >= 0 and y < self.num_cells_y:
                return True
        return False

    def draw_cells(self):
        for cell in self.cells:
            cell.draw(self, self.canvas)

    def make_exits(self):
        # TODO: Finish
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

        exit_node.remove_wall_dir(self.canvas, exit_node_delete_dir)
        start_node.remove_wall_dir(self.canvas, start_node_delete_dir)
        return

    def run(self):
        self.root.mainloop()

    def world_to_screen(self, x: float, y: float) -> (int, int):
        # converts a 0 - 1 value to a pixel coordinate value in screen space
        screen_x = ((x * (self.width - self.pixel_offset_x)) + self.pixel_offset_x)
        screen_y = ((y * (self.height - self.pixel_offset_y)) + self.pixel_offset_y)
        return screen_x, screen_y

    def to_image(self):
        # ensure window is updated to display tkinter canvas
        self.root.update()
        self.root.update_idletasks()
        # timing to ensure a snip can be obtained
        time.sleep(0.2)
        x = self.root.winfo_rootx() + self.canvas.winfo_x()
        y = self.root.winfo_rooty() + self.canvas.winfo_y()
        x1 = x + self.canvas.winfo_width()
        y1 = y + self.canvas.winfo_height()
        ImageGrab.grab().crop((x, y, x1, y1)).save(file_name)
        return

    def __del__(self):
        self.root.destroy()


if __name__ == '__main__':
    gen_random = False
    for token in sys.argv:
        if token == '-r':
            gen_random = True
    nodes = sys.argv[1]
    count = int(sys.argv[2])
    file_name_base = "maze"
    for i in range(count):
        file_name = file_name_base + str(i) + ".png"
        maze = MazeGen(file_name, int(nodes), gen_random)
        del maze



