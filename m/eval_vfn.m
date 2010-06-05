function [n_erased, n_steps, grid] = eval_vfn(tiles,w,is_echo)

if nargin<3, 
    is_echo = 0;
end;

X = 10;
Y = 20;
grid = zeros(Y+1+4,X+2+4);
grid(Y+1,:) = 8;
grid(:,1) = 8;
grid(:,X+2) = 8;

ep_end = 0;
n_steps = 0;
n_erased = 0;
top = get_skyline(grid);%%%%%%%
while ~ep_end,
    n_erased_old = n_erased;
    n_steps = n_steps + 1;
    r = unidrnd(7);

    n = 0;
    x_ofs_list = [];
    rot_list = [];
    v_list = [];

    top = get_skyline(grid);
    for x_ofs = 1:10,
        for rot = 1:4,
            tile = tiles(r).rotshape{rot};
            bottom = tiles(r).bottom{rot};
            gr = grid;
            [gr,y_ofs] = put_tile(gr, x_ofs, tile, bottom, top, r);
            if y_ofs>=0,
                [gr, n_er] = erase_lines(gr,y_ofs);
                phi = analyze_grid(gr,X,Y);

                n = n + 1;
                x_ofs_list(n) = x_ofs;
                rot_list(n) = rot;
                v_list(n) = w*phi';
            end;
        end;
    end;
    if n>0,
        [m, a] = min(v_list);
%        a = unidrnd(length(x_ofs_list));
        x_ofs = x_ofs_list(a);
        rot = rot_list(a);
        
        tile = tiles(r).rotshape{rot};
        bottom = tiles(r).bottom{rot};
        top = get_skyline(grid);
        [grid,y_ofs] = put_tile(grid, x_ofs, tile, bottom, top, r);
        [grid, n_er] = erase_lines(grid,y_ofs);
        n_erased = n_erased + n_er;
    else
        ep_end = 1;
    end;
    if is_echo & floor(n_erased/1000)~=floor(n_erased_old/1000),
        fprintf(1,'.');
    end;
%     if n_steps>30,
%         ep_end = 1;
%     end;
end;

