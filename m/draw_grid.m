function draw_grid(grid, c)

colors = 'rgbcrmykm';

figure(2);
clf;
hold on;
Y = size(grid,1)-4;
axis([1,size(grid,2)+1-4,0,size(grid,1)-4])
set(gca,'PlotBoxAspectRatio',[0.6 1 1]);


for i = 1:size(grid,2)-4,
    for j = 1:size(grid,1)-4,
        if grid(j,i)==0,
            color = 'w';
        else
            color = colors(grid(j,i));
        end;
        if color=='y'
            color = [0.8 0.8 0.8];
        end;
        if color == 'k'
            color = [0.3, 0.3,0.3];
        end;
        if color=='m'
            color = 'k';
        end;
        if (c ~= 0) & (color=='b')
            color = colors(c);
        end;
        rectangle('Position',[i,Y-j,1,1],'FaceColor',color, 'EdgeColor',[0.8 0.8 0.8]);
    end;
end;
axis off;
set(gcf,'Color',[1 1 1]);