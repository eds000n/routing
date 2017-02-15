library(Hmisc)
library(gplots)
rm(list=ls())
simulacao = "rrange"	#size,rrange, numevent, esize, ndensity, time
infraF = "~heitor/ns/sinalgo-0/logs/Infra/traces"
infraF = paste(infraF,simulacao,sep="-")
infraF = paste(infraF,"sumario.plt",sep="/")
infra=read.table(infraF,head=TRUE)
base="size"
legendaX=matrix(c("left"),nrow=1,ncol=5)
legendaY=matrix(c("top"),nrow=1,ncol=5)
datarate=60
slab="size"
if (simulacao == "size"){
	base = infra$NN
	infra$NE=1
	slab = "size (nodes)"
	legendaX=c("left","left","left","left","left") #node degree, node density, data packet, routing efficiency, overhead
	legendaY=c("center","center","top","top","top")
}
if (simulacao == "rrange"){
	base = infra$RRANGE;
	infra$NE=1
	slab = "Communication radius (m)"
	legendaX=c("left","left","right","right","right") #node degree, node density, data packet, routing efficiency, overhead
	legendaY=c("top","top","top","top","top") 
}
if (simulacao == "numevent"){
	base = infra$NE
	slab = "# of events"
	legendaX=c("left","left","left","left","left") #node degree, node density, data packet, routing efficiency, overhead
	legendaY=c("top","center","top","bottom","top")
}
if (simulacao == "esize"){
	base = infra$ES
	infra$NE=1
	slab = "Event radius (m)"
	legendaX=c("left","left","left","right","left") #node degree, node density, data packet, routing efficiency, overhead
	legendaY=c("top","center","top","top","top")
	
}
if (simulacao == "ndensity"){
	base = infra$NN
	infra$NE=1
	slab = "size (nodes)"
	legendaX=c("left","left","left","right","left") #node degree, node density, data packet, routing efficiency, overhead
	legendaY=c("top","top","top","top","top")
	
}
if (simulacao == "time"){
	base = infra$TIME/3600
	infra$NE=1
	slab = "Event duration (h)"
	legendaX=c("left","left","left","right","left") #node degree, node density, data packet, routing efficiency, overhead
	legendaY=c("center","center","top","top","center")
	
}

cnsF = "~heitor/ns/sinalgo-0/logs/CNS/traces"
cnsF = paste(cnsF,simulacao,sep="-")
cnsF = paste(cnsF,"sumario.plt",sep="/")
cns=read.table(cnsF,head=TRUE)

sptF = "~heitor/ns/sinalgo-0/logs/SPT/traces"
sptF = paste(sptF,simulacao,sep="-")
sptF = paste(sptF,"sumario.plt",sep="/")
spt=read.table(sptF,head=TRUE)

#parametros de visualizacao
linewd = 1.3
fsize = 1.1
fsizeL = 1
tsize = 1.2

paperh = 8.3 #tamanho do papel (altura)
paperw = 8.7 #tamanho do papel (largura)
nfigurascol = 2 #numero de figuras por coluna no texto

pch1 = 0
pch2 = 1
pch3 = 2
pch4 = 6 #apenas para o lower bound do gráfico data 

lty1 = 1 #infra
lty2 = 4 #cns	
lty3 = 6 #spt	
lty4 = 2 #apenas para o lower bound do gráfico data

#####################################################################################
##Node Degree x Source Nodes  - Communication Radius ################################
#####################################################################################
arquivo = paste("/Users/heitor/Documents/Doutorado/PaperInfra/",simulacao,sep="")
arquivo = paste(arquivo,"degree.eps",sep="-")
#eps(filename=arquivo)
postscript(arquivo,paper='special',height=paperh/nfigurascol,width=paperw/nfigurascol)
minimo=min(infra$DEGREE-infra$E_DG,infra$DETECT-infra$E_DET)
maximo=max(infra$DEGREE+infra$E_DG,infra$DETECT+infra$E_DET)
#plot(base,infra$DEGREE,xlab=slab,ylab='Nodes',type='o',pch=pch1,ylim=c(minimo,maximo),main="Node Degree and Source Degree",cex=tsize,cex.lab=fsize,cex.axis=fsize,cex.sub=fsize,lty=lty1,lwd=linewd)
plot(base,infra$DEGREE, xlab=slab, ylab='Nodes', type='o', pch=pch1, ylim=c(minimo,maximo), cex=tsize, cex.lab=fsize, cex.axis=fsize, lty=lty1, lwd=linewd)
lines(base,infra$DETECT,type='o',pch=pch2,cex=tsize,lty=lty2,lwd=linewd) 
smartlegend(x=legendaX[1],y=legendaY[1],c("Node Degree","Source Nodes"), lty=c(lty1,lty2),pch=c(pch1,pch2),pt.cex=tsize,lwd=linewd,cex=fsizeL)
errbar(base,infra$DEGREE,infra$DEGREE+infra$E_DG/2,infra$DEGREE-infra$E_DG/2,add=TRUE,pch=pch1,cex=tsize,lwd=linewd)			
errbar(base,infra$DETECT,infra$DETECT+infra$E_DET/2,infra$DETECT-infra$E_DET/2,add=TRUE,pch=pch2,cex=tsize,lwd=linewd)			
dev.off()

#####################################################################################
##Node density x  Source Nodes density  - Communication Radius ######################
#####################################################################################

arquivo = paste("/Users/heitor/Documents/Doutorado/PaperInfra/",simulacao,sep="")
arquivo = paste(arquivo,"sourcedensity.eps",sep="-")

#eps(filename=arquivo)
postscript(arquivo,paper='special',height=paperh/nfigurascol,width=paperw/nfigurascol)
minimo=min(infra$NDENSITY-infra$E_NDENSIT,infra$SNDENSITY-infra$E_SNDENSIT)
maximo=max(infra$SNDENSITY+infra$E_SNDENSIT,infra$NDENSITY+infra$E_NDENSIT)

#plot(base,(infra$SNDENSITY),xlab=slab,ylab='Node density',type='o',pch=pch1,ylim=c(minimo,maximo),main="Source density and Node density",cex=tsize,cex.lab=fsize,cex.axis=fsize,cex.sub=fsize,lty=lty1,lwd=linewd)

plot(base,(infra$SNDENSITY),xlab=slab,ylab='Node density', type='o', pch=pch1, ylim=c(minimo,maximo), cex=tsize, cex.lab=fsize, cex.axis=fsize, cex.sub=fsize, lty=lty1, lwd=linewd)
lines(base,infra$NDENSITY,type='o',pch=pch2,cex=tsize,lty=lty2,lwd=linewd) 
errbar(base,infra$SNDENSITY,infra$SNDENSITY+infra$E_SNDENSIT/2,infra$SNDENSITY-infra$E_SNDENSITY/2,add=TRUE,pch=pch1,cex=tsize,lwd=linewd)			
errbar(base,infra$NDENSITY,infra$NDENSITY+infra$E_NDENSITY/2,infra$NDENSITY-infra$E_NDENSITY/2,add=TRUE,pch=pch2,cex=tsize,lwd=linewd)			
smartlegend(x=legendaX[2],y=legendaY[2],c("Source density","Node density"), lty=c(lty1,lty2),pch=c(pch1,pch2),pt.cex=tsize,lwd=linewd,cex=fsizeL)
dev.off()

#######################################################################################
##Data Packets ########################################################################
#######################################################################################
lb = infra$RCVDATA*(infra$SKDIST) + (infra$DETECT - infra$NE)*min(infra$RCVDATA,infra$TIME/datarate)
infra$SNTPKT=infra$SNTPKT/1000
infra$E_SNT = infra$E_SNT/1000
cns$SNTPKT=cns$SNTPKT/1000
cns$E_SNT = cns$E_SNT/1000
spt$SNTPKT=spt$SNTPKT/1000
spt$E_SNT = spt$E_SNT/1000
lb = lb/1000
#lbdelta = infra$E_RCVDATA*(infra$E_SKDIST + (infra$E_DET -1))
minimo=min(infra$SNTPKT-infra$E_SNT,cns$SNTPKT-cns$E_SNT,lb,spt$SNTPKT-spt$E_SNT)
maximo=max(infra$SNTPKT+infra$E_SNT,cns$SNTPKT+cns$E_SNT,lb,spt$SNTPKT+spt$E_SNT)
#ylim=c(minimo,maximo),
arquivo = paste("/Users/heitor/Documents/Doutorado/PaperInfra/",simulacao,sep="")
arquivo = paste(arquivo,"data.eps",sep="-")

#eps(filename=arquivo)
postscript(arquivo,paper='special',height=paperh/nfigurascol,width=paperw/nfigurascol)
#plot(base,(infra$SNTPKT),xlab=slab,ylab='Packets',type='o',pch=pch1,main="Data Packet",ylim=c(minimo,maximo),cex=tsize,cex.lab=fsize,cex.axis=fsize,cex.sub=fsize,lty=lty1,lwd=linewd)

plot(base,(infra$SNTPKT), xlab=slab, ylab=expression(paste('Packets ',(x10^3))), type='o', pch=pch1, ylim=c(minimo,maximo), cex=tsize, cex.lab=fsize, cex.axis=fsize, cex.sub=fsize, lty=lty1, lwd=linewd)
lines(base,(cns$SNTPKT),type="o",pch=pch2,cex=tsize,lty=lty2,lwd=linewd)
lines(base,(spt$SNTPKT),type="o",pch=pch3,cex=tsize,lty=lty3,lwd=linewd)
lines(base,lb,type="o",pch=pch4,cex=tsize,lty=lty4,lwd=linewd)
errbar(base,infra$SNTPKT,infra$SNTPKT+infra$E_SNT,infra$SNTPKT-infra$E_SNT,add=TRUE,pch=pch1,cex=tsize,lwd=linewd)
errbar(base,cns$SNTPKT,cns$SNTPKT+cns$E_SNT,cns$SNTPKT-cns$E_SNT,add=TRUE,pch=pch2,cex=tsize,lwd=linewd)
errbar(base,spt$SNTPKT,spt$SNTPKT+spt$E_SNT,spt$SNTPKT-spt$E_SNT,add=TRUE,pch=pch3,cex=tsize,lwd=linewd)
#errbar(base,lb,lb+lbdelta/2,lb-lbdelta/2,add=TRUE,pch=pch4)
smartlegend(x=legendaX[3],y=legendaY[3],c("InFRA","CNS","SPT","Lower bound"), lty=c(lty1,lty2,lty3,lty4),pch=c(pch1,pch2,pch3,pch4),pt.cex=tsize,lwd=linewd,cex=fsizeL)
dev.off()


###################################################################################################
##Routing Efficiency (energy efficiency) ##########################################################
###################################################################################################

arquivo = paste("/Users/heitor/Documents/Doutorado/PaperInfra/",simulacao,sep="")
arquivo = paste(arquivo,"efficiency.eps",sep="-")

#eps(filename=arquivo)
postscript(arquivo,paper='special',height=paperh/nfigurascol,width=paperw/nfigurascol)

minimo=min(infra$REFFICIENCY-infra$E_REFFICIENCY,cns$REFFICIENCY-cns$E_REFFICIENCY,spt$REFFICIENCY-spt$E_REFFICIENCY)
maximo=max(infra$REFFICIENCY+infra$E_REFFICIENCY,cns$REFFICIENCY+cns$E_REFFICIENCY,spt$REFFICIENCY+spt$E_REFFICIENCY)
#plot(base,infra$REFFICIENCY,xlab=slab,ylab='Packets per data processed',type='o',pch=pch1,main="Routing Efficiency",ylim=c(minimo,maximo),cex=tsize,cex.lab=fsize,cex.axis=fsize,cex.sub=fsize,lty=lty1,lwd=linewd)
plot(base,infra$REFFICIENCY, xlab=slab, ylab='Packets per data processed', type='o', pch=pch1, ylim=c(minimo,maximo), cex=tsize, cex.lab=fsize, cex.axis=fsize, cex.sub=fsize, lty=lty1, lwd=linewd)
lines(base,cns$REFFICIENCY,type="o",pch=pch2,cex=tsize,lty=lty2,lwd=linewd)
lines(base,spt$REFFICIENCY,type="o",pch=pch3,cex=tsize,lty=lty3,lwd=linewd)
errbar(base,infra$REFFICIENCY,infra$REFFICIENCY+infra$E_REFFICIENCY,infra$REFFICIENCY-infra$E_REFFICIENCY,add=TRUE,pch=pch1,cex=tsize,lwd=linewd)
errbar(base,cns$REFFICIENCY,cns$REFFICIENCY+cns$E_REFFICIENCY,cns$REFFICIENCY-cns$E_REFFICIENCY,add=TRUE,pch=pch2,cex=tsize,lwd=linewd)
errbar(base,spt$REFFICIENCY,spt$REFFICIENCY+spt$E_REFFICIENCY,spt$REFFICIENCY-spt$E_REFFICIENCY,add=TRUE,pch=pch3,cex=tsize,lwd=linewd)
smartlegend(x=legendaX[4],y=legendaY[4],c("InFRA","CNS","SPT"), lty=c(lty1,lty2,lty3),pch=c(pch1,pch2,pch3),pt.cex=tsize,lwd=linewd,cex=fsizeL)
dev.off()


####################################################################################################
##Packet Overhead ##################################################################################
####################################################################################################

infra$CHOVER = infra$CHOVER/1000
infra$E_CHOVER = infra$E_CHOVER/1000
cns$CHOVER = cns$CHOVER/1000
cns$E_CHOVER = cns$E_CHOVER/1000
spt$CHOVER = spt$CHOVER/1000
spt$E_CHOVER = spt$E_CHOVER/1000

minimo=min(infra$CHOVER-infra$E_CHOVER,cns$CHOVER-cns$E_CHOVER,spt$CHOVER-spt$E_CHOVER)
maximo=max(infra$CHOVER+infra$E_CHOVER,cns$CHOVER+cns$E_CHOVER,spt$CHOVER+spt$E_CHOVER)
arquivo = paste("/Users/heitor/Documents/Doutorado/PaperInfra/",simulacao,sep="")
arquivo = paste(arquivo,"overhead.eps",sep="-")

#eps(filename=arquivo)
postscript(arquivo,paper='special',height=paperh/nfigurascol,width=paperw/nfigurascol)

#plot(base,(infra$CHOVER),xlab=slab,ylab='Packets',type='o',pch=pch1,main="Packet Overhead",ylim=c(minimo,maximo),cex=tsize,cex.lab=fsize,cex.axis=fsize,cex.sub=fsize,lty=lty1,lwd=linewd)
plot(base,(infra$CHOVER), xlab=slab,ylab=expression(paste('Packets ',(x10^3))), type='o', pch=pch1, ylim=c(minimo,maximo), cex=tsize, cex.lab=fsize, cex.axis=fsize, cex.sub=fsize, lty=lty1, lwd=linewd)
lines(base,(cns$CHOVER),type="o",pch=pch2,cex=tsize,lty=lty2,lwd=linewd)
lines(base,(spt$CHOVER),type="o",pch=pch3,cex=tsize,lty=lty3,lwd=linewd)

errbar(base,(infra$CHOVER),(infra$CHOVER)+(infra$E_CHOVER),(infra$CHOVER)-(infra$E_CHOVER),add=TRUE,pch=pch1,cex=tsize,lwd=linewd)
errbar(base,(cns$CHOVER),(cns$CHOVER)+(cns$E_CHOVER),(cns$CHOVER)-(cns$E_CHOVER),add=TRUE,pch=pch2,cex=tsize,lwd=linewd)
errbar(base,(spt$CHOVER),(spt$CHOVER)+(spt$E_CHOVER),(spt$CHOVER)-(spt$E_CHOVER),add=TRUE,pch=pch3,cex=tsize,lwd=linewd)

smartlegend(x=legendaX[5],y=legendaY[5],c("InFRA","CNS","SPT"), lty=c(lty1,lty2,lty3),pch=c(pch1,pch2,pch3),pt.cex=tsize,lwd=linewd,cex=fsizeL)
dev.off()


