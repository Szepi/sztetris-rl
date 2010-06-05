function [grid,y_ofs] = put_tile(grid, x_ofs, tile, bottom, top, r)

z = top((1:4)+x_ofs) - bottom;
y_ofs = min(z)-1;
if y_ofs>=0,
    grid(y_ofs+(1:4),x_ofs+(1:4)) = grid(y_ofs+(1:4),x_ofs+(1:4)) + tile*r;
end;

% y_ofs = 0; % +1-gyel probalkozunk
% gr1 = grid(y_ofs+(1:4),x_ofs+(1:4));
% while ~any(gr1(:) & tile(:)),
%     y_ofs = y_ofs+1;
%     gr1 = grid(y_ofs+(1:4),x_ofs+(1:4));
% end;    
% y_ofs = y_ofs-1;
% if y_ofs>=0,
%     gr1 = grid(y_ofs+(1:3),x_ofs+(1:4));
%     tile1 = tile(2:4,:);
%     if any(any(gr1(:) & tile1(:))),
%         y_ofs = -1;
%     else
%     grid(y_ofs+(1:4),x_ofs+(1:4)) = grid(y_ofs+(1:4),x_ofs+(1:4)) + tile*r;
%     end;
% end;
