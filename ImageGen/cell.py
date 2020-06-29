from MazeGen import MazeGen
from tkinter import *
from PIL import Image, ImageDraw

class Cell:
    top = 0
    right = 1
    bottom = 2
    left = 3
    num_directions = 4

    def __init__(self, x_pos: float, y_pos: float):
        self.edges = []
        self.x_pos = x_pos
        self.y_pos = y_pos
        self.visited = False
        self.neighbors = []
        self.neighbor_lookup = {}
        self.wall_state = [True] * 4
        self.render_walls = []
        self.center_render_component = None

    def add_neighbor(self, other):
        if self not in other.neighbors and other not in self.neighbors:
            # self.neighbors.append(other)
            # other.neighbors.append(self)
            self.neighbor_lookup[self.get_relative_direction(other)] = other
            other.neighbor_lookup[other.get_relative_direction(self)] = self

    def draw(self, maze: MazeGen, canvas: Canvas):
        # draw 4 lines for the cell centered around the point x_pos, y_pos
        offset_x = maze.wall_length_x
        offset_y = maze.wall_length_y
        x0, y0 = maze.world_to_screen(self.x_pos, self.y_pos)
        x1, y1 = maze.world_to_screen(self.x_pos, self.y_pos)

        # top line
        self.render_walls.append(canvas.create_line(x0 + offset_x, y0 - offset_y, x1 - offset_x, y0 - offset_y))

        # right line
        self.render_walls.append(canvas.create_line(x0 + offset_x, y0 + offset_y, x1 + offset_x, y0 - offset_y))

        # bottom line
        self.render_walls.append(canvas.create_line(x0 - offset_x, y0 + offset_y, x1 + offset_x, y0 + offset_y))

        # left line
        self.render_walls.append(canvas.create_line(x0 - offset_x, y0 + offset_y, x1 - offset_x, y0 - offset_y))

        #self.render_walls = canvas.create_rectangle(x0 - maze.wall_length_x, y0 - maze.wall_length_y, x1 + maze.wall_length_x, y1 + maze.wall_length_y)

        #self.draw_center(maze, canvas)

    def draw_center(self, maze: MazeGen, canvas: Canvas):
        x0, y0 = maze.world_to_screen(self.x_pos, self.y_pos)
        self.center_render_component = canvas.create_oval(x0 - maze.pixel_center, y0 - maze.pixel_center, x0 + maze.pixel_center, y0+ maze.pixel_center, fill="Black")
        return

    def get_relative_direction(self, other):
        if self.x_pos == other.x_pos and self.y_pos == other.y_pos:
            return None
        elif self.x_pos == other.x_pos and self.y_pos > other.y_pos:
            return Cell.top
        elif self.x_pos < other.x_pos and self.y_pos == other.y_pos:
            return Cell.right
        elif self.x_pos == other.x_pos and self.y_pos < other.y_pos:
            return Cell.bottom
        else:
            return Cell.left

    def color_neighbors(self, canvas:Canvas):
        for neighbor in self.neighbors:
            canvas.itemconfigure(neighbor.center_render_component, fill="red")

    def color(self, canvas:Canvas, color):
        canvas.itemconfigure(self.center_render_component, fill=color)

    def remove_wall(self, canvas: Canvas, other: object):
        self.remove_wall(canvas, self.get_relative_direction(other))
        return

    # Cells are indexed as follows: [0] - top, [1] - right, [2] - bottom, [3] - left
    def remove_wall(self, canvas: Canvas, other: object):
        direction = self.get_relative_direction(other)
        neighbor = self.get_neighbor(direction)
        if neighbor is not None:
            # remove wall in opposite direction if there is a neighbor
            self.remove_wall_pair(canvas, direction, neighbor)
        else:
            self._remove_wall(canvas, direction)
        return

    def remove_wall_pair(self, canvas, direction, other):
        # remove this wall
        canvas.itemconfigure(self.render_walls[direction], fill="white")
        # remove paired wall
        canvas.itemconfigure(other.render_walls[(direction + 2) % Cell.num_directions], fill="white")
        return

    def _remove_wall(self, canvas: Canvas, direction):
        canvas.itemconfigure(self.render_walls[direction], fill="white")

    def get_neighbor(self, direction):
        return self.neighbor_lookup.get(direction)