function learn_tetris

% close all;
% clear all;
disp('******');

tiles = init_tiles;

alpha = 0.8;
rho = 0.2;
max_ep = 50;
wdim = 22;
erased_avg_list = [];
erased_elite_list = [];
steps_avg_list = [];
steps_elite_list = [];

M = ones(1,wdim)*0.0;
M(2) = 10;
M(3) = 1;
S = ones(1,wdim)*100;

for ce_it = 1:5000,
    disp(sprintf('\n--------\n CE-it: %d \n',ce_it));
    
    steps_list = [];
    erased_list = [];
    w_list = {};
    for i = 1:max_ep,
        w = M + sqrt(S).*randn(1,wdim);
        %    w = randn(1,wdim)*100;
        [n_erased, n_steps, grid] = eval_vfn(tiles,w);
        disp(sprintf('#%d: \t %d \t erased: %d \t meanS: %.1f',i,n_steps,n_erased,mean(S(2:22))));
        steps_list(i) = n_steps;
        erased_list(i) = n_erased;
        w_list{i} = w;
    end;
    
    [steps_list,order] = sort(-steps_list);
    steps_list = -steps_list;
    erased_list = erased_list(order);
%     [erased_list,order] = sort(erased_list,'descend');
%     steps_list = steps_list(order);
    m = zeros(size(M));
    s = zeros(size(S));
    n_elite = max_ep*rho;

    for i = 1:n_elite,
        m = m + w_list{order(i)};
    end;
    m = m/n_elite;
    for i = 1:n_elite,
        s = s + (w_list{order(i)}-m).^2;
    end;
    s = s/n_elite;
    M = M + alpha*(m-M);
    S = S + alpha*(s-S);

    erased_avg_list = [erased_avg_list, mean(erased_list)];
    erased_elite_list = [erased_elite_list, mean(erased_list(1:n_elite))];
    steps_avg_list = [steps_avg_list, mean(steps_list)];
    steps_elite_list = [steps_elite_list, mean(steps_list(1:n_elite))];
    if mod(ce_it,1)==0,
        fname = sprintf('results\\tetris_alpha08_%04d.mat',ce_it);
        save(fname);
    end;
end;

%draw_grid(grid);



% for it = 1:10,
%     r = unidrnd(7);
% 
%     rot = unidrnd(4);
%     x_ofs = unidrnd(10);
% 
%     tile = tiles(r).rotshape{rot};
%     [grid,y_ofs] = put_tile(grid, x_ofs, tile, r);
%     if y_ofs>=0,
%         [grid, n_erased] = erase_lines(grid,y_ofs);
%     end;
% end;


