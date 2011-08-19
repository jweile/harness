#!/usr/bin/Rscript

pdf("plot.pdf",width=4,height=8)
op=par(mfrow=c(3,1))

fn <- read.table("fn.tsv",header=TRUE,row.names=1,sep="\t")
barplot(as.matrix(t(fn)),beside=TRUE,xlab="# experiments",ylab=expression(E(L^{"("+")"})) )
legend("topright",c(expression(paste("Lee ",italic("et al."))),expression(paste("James ",italic("et al."))),expression(paste("Weile ",italic("et al.")))),bty="n",fill=grey.colors(3))

fp <- read.table("fp.tsv",header=TRUE,row.names=1,sep="\t")
barplot(as.matrix(t(fp)),beside=TRUE,xlab="# experiments",ylab=expression(E(L^{"("-")"})) )
#legend("right",colnames(fp),bty="n",fill=grey.colors(3))

er <- read.table("erloss.tsv",header=TRUE,row.names=1,sep="\t")
barplot(as.matrix(t(er)),beside=TRUE,xlab="# experiments",ylab=expression(E(L["ER"])) )
#legend("right",colnames(er),bty="n",fill=grey.colors(3))

par(op)
dev.off()

