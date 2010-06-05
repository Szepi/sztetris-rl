close all
tiles = init_tiles;

X = 10;
Y = 20;
grid = zeros(Y+1+4,X+2+4);
grid(Y+1,:) = 8;
grid(:,1) = 8;
grid(:,X+2) = 8;

commands = [[2,1,1,0]; [2,1,1,0]; [2,1,1,0]; [2,1,3,0]; [2,1,3,0]; [2,1,3,0]; [2,1,5,0]; [3,1,7,0];   [3,1,7,0];  [3,1,9,0];   [3,1,9,0]; [0,0,0,0]];  
commands = [commands; [3,1,9,1]];
commands = [commands; [3,1,5,9]];
commands = [commands; [3,1,3,3]];
 
for i = 1: size(commands,1);
    r=commands(i,1);
    rot=commands(i,2);
    x_ofs = commands(i,3);
    if (r==0)
        grid(grid>0 & grid<8) = 7;
        draw_grid(grid,0)
        continue;
    end;
    top = get_skyline(grid);
    tile = tiles(r).rotshape{rot};
    bottom = tiles(r).bottom{rot};
    [grid,y_ofs] = put_tile(grid, x_ofs, tile, bottom, top, r);
    [grid, n_er] = erase_lines(grid,y_ofs);
    draw_grid(grid, 0)
end;
