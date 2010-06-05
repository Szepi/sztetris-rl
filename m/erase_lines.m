function [grid, n_erased] = erase_lines(grid,y_ofs)

n_erased = 0; 

X = size(grid,2)-2-4;

for j = y_ofs+1:y_ofs+4,
%    if all(grid(j,2:X+1)) & ~all(grid(j,2:X+1)==8),
    if all(grid(j,2:X+1)) & (grid(j,5)~=8),
        grid(j,:) = [];
        grid = [zeros(1,X+6);grid];
        grid(1,1) = 8;
        grid(1,X+2) = 8;
        n_erased = n_erased+1;
%        disp('Juhejj');
    end;
end;