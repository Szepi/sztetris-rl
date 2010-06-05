
% for i=1:10,
%     lambdalist(i) = 1.0 - 1.0/i;
% end;
% lambdalist(11) = 1;
% 
% for i=1:length(lambdalist)
%     lambda = lambdalist(i)
%     fname = sprintf('../BI_lambda%.4f_eval.txt',lambda);
% 
%     f = fopen(fname);
%     N = fscanf(f,'%d',1);
%     avg = fscanf(f,'%f',1);
%     fclose(f);
%     
%     perf(i) = avg;
% end;
a = gca;
set(a,'FontSize',20);
hl = plot(lambdalist,perf,'k-x','LineWidth',2);
set (hl,'MarkerSize',10);
xlabel('lambda');
ylabel('score');
ylim([0,110]);
h= gcf;
set(h,'Color',[1 1 1]);
set(a,'PlotBoxAspectRatio',[1 0.5 1]);
