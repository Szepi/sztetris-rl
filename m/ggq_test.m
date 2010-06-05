index = 7;
fname = sprintf('run%03d.txt',index);
f = fopen(fname);
npars = fscanf(f,'%d\n',1);
disp(sprintf('Index: %03d\nParams:\n' ,index) );
for i=1:npars,
    pars{i} = fgetl(f);
    disp([pars{i}]);
end;
nweights = fscanf(f,'%d\n',1);
M = nweights*2+3;
rawdata = fscanf(f,'%f ',Inf);
rawdata = reshape(rawdata,M,length(rawdata)/M)';

tnorm = rawdata(:,2);
wnorm = rawdata(:,3);
figure; clf; hold on; 
plot(tnorm,'r');
plot(wnorm,'k');