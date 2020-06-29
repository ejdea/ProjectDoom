Usage:

node_x: number of cells in X dir
node_y: number of cells in Y dir
mode:
	-image - output raw images
	-canvas - render image to window
	-both - output raw image and render (not recommended for normal usage)

count: number of pictures to generate
-r: generate with random cells 

python3 MazeGen.py [nodes_x] [nodes_y] [mode: -image, -canvas, -both] [count] [optional: -r]


Current Bugs:
---tentative fix, but need more tests
	Some values of nodes cause the Pillow DrawLine image to incorrectly draw a line.
	For now, check the rendered images to ensure no artifacts are in the picture
