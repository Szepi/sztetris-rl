function top = get_skyline(grid)

for i = 1:size(grid,2),
    [m,k] = min(grid(:,i)==0);
    top(i) = k;
end;
