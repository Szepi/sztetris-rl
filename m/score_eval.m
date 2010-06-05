% close all;
% run = 0;
% type = 'BI';
% colors = 'rkbg';
% for sn=1:3,
%     fname = sprintf('../CE_%s_run%02s_s%d_eval.txt',type,run,sn-1)
%     [scores, freqs, runavg] = loadscores(fname);
%     figure(1); hold on;
%     plot(freqs, colors(sn),'LineWidth',2)
%     figure(2); hold on;
%     sd = 1.96*std(scores)./sqrt(1:length(scores))';    
%     plot(runavg, colors(sn),'LineWidth',2)
%     plot(runavg-sd, colors(sn),'LineWidth',1)
%     plot(runavg+sd, colors(sn),'LineWidth',1)
%     axis([0,1500,60,120]);
% end;

h1 = figure(2);
set(h1,'Color',[1,1,1]);
a1 = gca;
set(a1,'FontSize',20);

h1 = figure(1);
set(h1,'Color',[1,1,1]);
a1 = gca;
set(a1,'FontSize',20);
