# Author: Martin Edmunds
# Date: 06/29/2020
# Version 1.1

from MazeGen import MazeGen
from tkinter import *
from PIL import Image, ImageDraw


class Cell:

    # Direction Enums
    top = 0
    right = 1
    bottom = 2
    left = 3
    num_directions = 4

    # Draw Mode Enums
    canvas_mode = 5
    image_mode = 6
    both_mode = 7

    # Default drawing mode is image_mode ( 6 )
    def __init__(self, x_pos: float, y_pos: float, maze: MazeGen, canvas: Canvas, draw: ImageDraw, mode=6):
        # graph information
        self.x_pos = x_pos
        self.y_pos = y_pos
        self.neighbor_lookup = {}
        self.visited = False

        # Render information
        self.wall_state = [True] * 4
        self.render_walls = []
        self.center_render_component = None
        self.draw_lookup = {}
        self.draw_mode = mode

        # parent handles
        self.canvas = canvas
        self.maze = maze
        self.image_draw = draw
        self.wall_coords = []

        # generate wall coords on creation
        self.create_wall_coords()

    def create_wall_coords(self):
        offset_x = self.maze.wall_length_x
        offset_y = self.maze.wall_length_y
        x0, y0 = self.maze.world_to_screen(self.x_pos, self.y_pos)

        # [x0, y0, x1, y1]
        # top line
        self.wall_coords.append([x0 + offset_x, y0 - offset_y, x0 - offset_x, y0 - offset_y])
        # right line
        self.wall_coords.append([x0 + offset_x, y0 + offset_y, x0 + offset_x, y0 - offset_y])
        # bottom line
        self.wall_coords.append([x0 - offset_x, y0 + offset_y, x0 + offset_x, y0 + offset_y])
        # left line
        self.wall_coords.append([x0 - offset_x, y0 + offset_y, x0 - offset_x, y0 - offset_y])

    def add_neighbor(self, other):
        if self not in other.neighbor_lookup and other not in self.neighbor_lookup:
            self.neighbor_lookup[self.get_relative_direction(other)] = other
            other.neighbor_lookup[other.get_relative_direction(self)] = self

    def draw_cell(self):
        if self.draw_mode == Cell.canvas_mode:
            self._draw_cell_canvas()
        elif self.draw_mode == Cell.image_mode:
            self._draw_cell_image()
        else:
            self._draw_cell_canvas()
            self._draw_cell_image()

    def _draw_cell_canvas(self):
        """
        Draws into the tkinter canvas
        :return:
        """
        # draw 4 lines for the cell centered around the point x_pos, y_pos
        # top line
        if self.wall_state[Cell.top]:
            self.render_walls.append(
                self.canvas.create_line(self.wall_coords[0][0], self.wall_coords[0][1], self.wall_coords[0][2],
                                        self.wall_coords[0][3]))

        # right line
        if self.wall_state[Cell.right]:
            self.render_walls.append(
                self.canvas.create_line(self.wall_coords[1][0], self.wall_coords[1][1], self.wall_coords[1][2],
                                        self.wall_coords[1][3]))

        # bottom line
        if self.wall_state[Cell.bottom]:
            self.render_walls.append(
                self.canvas.create_line(self.wall_coords[2][0], self.wall_coords[2][1], self.wall_coords[2][2],
                                        self.wall_coords[2][3]))

        # left line
        if self.wall_state[Cell.left]:
            self.render_walls.append(
                self.canvas.create_line(self.wall_coords[3][0], self.wall_coords[3][1], self.wall_coords[3][2],
                                        self.wall_coords[3][3]))

    def _draw_cell_image(self):
        """
        Draws into the PIL Image.
        Current Bug: For some values of cells the lines are not drawn correctly. They are still drawn correctly
        on the tkinter canvas, so I assume it's something to do with the pixel-fill algorithm used in Pillow's line draw

        :return:
        """
        # draw 4 lines for the cell centered around the point x_pos, y_pos
        # top line
        if self.wall_state[Cell.top]:
            self.image_draw.line(self.wall_coords[Cell.top], MazeGen.black)

        # right line
        if self.wall_state[Cell.right]:
            self.image_draw.line(self.wall_coords[Cell.right], MazeGen.black)

        # bottom line
        if self.wall_state[Cell.bottom]:
            self.image_draw.line(self.wall_coords[Cell.bottom], MazeGen.black)

        # left line
        if self.wall_state[Cell.left]:
            self.image_draw.line(self.wall_coords[Cell.left], MazeGen.black)

    def draw_center(self, maze: MazeGen):
        """
        Debug function, draws center of node
        :param maze:
        :return:
        """
        x0, y0 = maze.world_to_screen(self.x_pos, self.y_pos)
        self.center_render_component = self.canvas.create_oval(x0 - maze.pixel_center, y0 - maze.pixel_center, x0 + maze.pixel_center, y0+ maze.pixel_center, fill="Black")
        return

    def get_relative_direction(self, other):
        """
        Returns relative direction of a node
        :param other: node to check relation
        :return: direction
        """
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

    def color_neighbors(self):
        """
        Debug Function
        :return:
        """
        for neighbor in self.neighbor_lookup:
            self.canvas.itemconfigure(neighbor.center_render_component, fill="red")

    def color(self, color):
        """
        Debug Function
        :param color:
        :return:
        """
        self.canvas.itemconfigure(self.center_render_component, fill=color)

    def remove_wall_dir(self, direction: int):
        self._remove_wall(self, direction)

    # Cells are indexed as follows: [0] - top, [1] - right, [2] - bottom, [3] - left
    def remove_wall_node(self, other: object):
        direction = self.get_relative_direction(other)
        neighbor = self.get_neighbor(direction)
        if neighbor is not None:
            # remove wall in opposite direction if there is a neighbor
            self._remove_wall_pair(direction, neighbor)
        else:
            self._remove_wall(direction)
        return

    def _remove_wall_pair(self, direction, other):
        """
        Helper Function that removes a wall pair between two nodes
        :param direction: relative direction
        :param other: Cell to remove wall between
        :return:
        """
        # remove this wall
        self._remove_wall(self, direction)
        # remove other nodes paired wall
        other._remove_wall(other, (direction + 2) % Cell.num_directions)
        return

    def _remove_wall(self, node, direction):
        """
        Helper function that removes a wall from a direction
        :param direction: relative direction
        :return:
        """
        node.wall_state[direction] = False
        if self.draw_mode == Cell.canvas_mode:
            self.canvas.itemconfigure(node.render_walls[direction], fill="white")
        elif self.draw_mode == Cell.image_mode:
            # draw line as white
            self.image_draw.line(self.wall_coords[direction], MazeGen.white)
            self._draw_cell_image()
        else:
            self.canvas.itemconfigure(node.render_walls[direction], fill="white")
            self.image_draw.line(self.wall_coords[direction], MazeGen.white)
            self._draw_cell_image()
        return

    def get_neighbor(self, direction):
        """
        Returns a neighbor node relative to the direction passed
        :param direction: Expected node direction ie. Cell.left, Cell.right...
        :return: Cell
        """
        return self.neighbor_lookup.get(direction)