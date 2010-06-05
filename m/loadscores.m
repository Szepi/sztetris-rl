function [ scores, freqs, runavg ] = loadscores( fname )

f = fopen(fname);
N = fscanf(f,'%d',1);
avg = fscanf(f,'%f',1);
scores = fscanf(f,'%f',Inf);
fclose(f);

M = max(scores);
freqs = zeros(M,1);
for i=1:N,
    freqs(scores(i)) = freqs(scores(i))+1;
end;

runavg = cumsum(scores)./(1:N)';

end

