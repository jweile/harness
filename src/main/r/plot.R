#!/usr/bin/Rscript

pdf("fig1.pdf",width=4,height=8)
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

pdf("fig2.pdf",width=6,height=9)
op=par(mfrow=c(2,1))
probsppi = read.csv("probabilities_ppi.csv")
hist(probsppi[,1],main="",xlab="PPI probability",col="gray")
probsgi = read.csv("probabilities_sl.csv")
hist(probsgi[,1],main="",xlab="Synthetic GI probability",col="gray")
par(op)
dev.off()

