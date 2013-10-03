#sokoban

An attempt to create online Player vs Player sokoban game with different modes of playing and automatic level generation.

## TODO
First thing to be made is the automatic level generation
### Automatic level generation
The algorithm is as follows:
1) Initialize new field, where all squares are walls  
2) Put few completed goals on the field  
3) Put (virtual) player at one of the completed goals' neighbours  
4) Pull the box random times at random directions (as you walk over the walls, you make them empty squares)  
5) Go to the next box  
6) Repeat 4) and 5) until all the boxes are moved  

### WEB app where you can play the server side generated levels on demand

### PvP integration in the server

## Contribute
Just make a pull request with the feature you've created
