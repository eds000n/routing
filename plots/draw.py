#!/usr/bin/python

import sys, getopt, os
import matplotlib.pyplot as plt
import numpy as np

def adderrorbar(x, y, yerror, lname):
    plt.errorbar(x, y, yerr=yerror, label=lname)
    plt.grid(True)

def adderrorbarnorm(x, y, yerror, lname):
    m = max(y)
    y=y*100/m
    yerror=yerror/m
    plt.errorbar(x, 100-y, yerr=yerror, label=lname)
    plt.grid(True)

#def embededsubploterrorbarnorm2(ax1, ax2, x,y, yerror, lname, xs, ys, yserror, lsname, where):
def embededsubploterrorbarnorm2(x,y, yerror, lname, where):
    if where <= 9:
        #m = max(y)
        #m =1024
        #m = [] 
        #for i in y:
        #    m.append(1024)
        #m = list(1024
        #print "Number of nods ", m
        y=y*100/1024
        yerror=np.array(yerror)/1024
        #ax1.errorbar(x, 100-y, yerr=yerror, label=lname)
        #ax1.grid(True)
        #plt.set_xticklabels( ['2', '4', '6', '8', '10', '12', '14', '16', '18'] )
        plt.xticks([2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000] , ['2', '4', '6', '8', '10', '12', '14', '16', '18'])
        plt.errorbar(x, 100-y, yerr=yerror, label=lname)
        plt.grid(True)
    '''else:
        ms = max(ys, 1024)
        #m=1024
        print "Number of nods ", ms
        ys=ys*100/ms
        yserror=yserror/ms
        if where == 4:
            c='cyan'
        elif where == 5:
            c='magenta'
        elif where == 6:
            c='yellow'
        ax1.errorbar(xs, 100-ys, yerr=yserror, label=lsname, color=c)
        ax2.errorbar(xs, 100-ys, yerr=yserror, label=lsname, color=c)
        ax2.grid(True)
        ax2.set_xlim(3000, 20000)
        #ax2.set_xticks([2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000], ['2', '3', '4', '5', '6', '7', '8', '9', '10'])
        ax2.set_xticks([3600, 7200, 10800, 14400, 18000] )
        ax2.set_xticklabels(['2', '4', '6', '8', '10'])
        #ax2.set_xticks([2, 3, 4, 5, 6, 7, 8, 9, 10])'''

def embededsubploterrorbarnorm(ax1, ax2, x,y, yerror, lname, xs, ys, yserror, lsname, where):
    if where <= 4:
        m = max(y)
        #m=1024
        print "Number of nods ", m
        y=y*100/m
        yerror=yerror/m
        ax1.errorbar(x, 100-y, yerr=yerror, label=lname)
        ax1.grid(True)
    else:
        ms = max(ys)
        #m=1024
        print "Number of nods ", ms
        ys=ys*100/ms
        yserror=yserror/ms
        if where == 5:
            c='magenta'
        elif where == 6:
            c='yellow'
        elif where == 7:
            c='black'
        ax1.errorbar(xs, 100-ys, yerr=yserror, label=lsname, color=c)
        ax2.errorbar(xs, 100-ys, yerr=yserror, label=lsname, color=c)
        ax2.grid(True)
        ax2.set_xlim(4000, 10000)
        ax1.set_ylim(0, 40)
        #ax2.set_xticks([2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000], ['2', '3', '4', '5', '6', '7', '8', '9', '10'])
        
        #ax2.set_xticks([2000, 4000, 6000, 8000, 10000] )
        #ax2.set_xticklabels( ['2', '4', '6', '8', '10'] )
        #ax1.set_xticklabels( ['2', '4', '6', '8', '10'] )
        #ax1.set_xticks([2000, 4000, 6000, 8000, 10000] )
        ax2.set_xticks([2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000] )
        ax2.set_xticklabels( ['2', '4', '6', '8', '10', '12', '14', '16', '18', '20'] )
        ax1.set_xticklabels( ['2', '4', '6', '8', '10', '12', '14', '16', '18', '20'] )
        ax1.set_xticks([2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000] )
        
        #ax2.set_xticklabels(['2', '4', '6', '8', '10'])
        #ax2.set_xticks([2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000] )
        #ax2.set_xticks([2, 4, 6, 8, 10, 12, 14, 16, 18, 20] )
        #ax2.set_xticklabels(['2', '4', '6', '8', '10', '12', '14', '16', '18', '20'])
        #ax2.set_xticks([2, 3, 4, 5, 6, 7, 8, 9, 10])

def addplot(x, y, lname):
    plt.plot(x, y, label=lname)
    plt.grid(True)

def addnormplot(x, y, lname):
    m = max(y)
    y=y*100/m
    plt.plot(x, 100-y, label=lname)
    plt.grid(True)

def addladder(x, y, lname):
    plt.plot(x, y, drawstyle="steps", label=lname)
    plt.grid(True)

#def addenergymap(x, y, z):
#    

def usage():
    os.system("cat README")
    sys.exit()

def main():
    save = False
    label_x = ""
    label_y = ""
    save_in = ""
    title = ""
    type_g = 0
    if len(sys.argv)==1:
        usage()
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hT:t:x:y:s:")
    except getopt.GetoptError as err:
        print str(err)
        usage()
        sys.exit(2)
    for o, a in opts:
        if o == "-t":
            title=a
        elif o == "-T":
            type_g = int(a)
        elif o == "-h":
            usage()
        elif o == "-x":
            label_x = a
        elif o == "-y":
            label_y = a
        elif o == "-s":
            save_in = a
            save = True

    xmin=0
    xmax=0
    if type_g == 5:
        fig, ax1 = plt.subplots()
        left, bottom, width, height = [ 0.18, 0.47, 0.38, 0.38 ]
        ax2 = fig.add_axes([left, bottom, width, height])

    i=0
    for a in args:
        i=i+1
        label=".".join(a.split("data")[0].split(".")[1:-1])
        print "Processing file ", a
        if type_g == 0:         #normal plot
            x, y = np.loadtxt(a, unpack=True)
            xmin=min(x)
            xmax=max(x)
            addplot(x,y,label)
        elif type_g == 1:       #errorbar plot
            x, y, ymin, ymax =np.loadtxt(a, unpack=True)
            xmin=min(x)
            xmax=max(x)
            yerror=[ymin, ymax]
            adderrorbar(x,y,yerror,label)
        elif type_g == 2:       #ladder
            x, y =np.loadtxt(a, unpack=True)
            xmin=min(x)
            xmax=max(x)
            addladder(x,y,label)
        elif type_g == 3:
            x, y =np.loadtxt(a, unpack=True)
            xmin=min(x)
            xmax=max(x)
            addnormplot(x,y,label)
        elif type_g == 4:   #errorbar normalized plot
            x, y, ymin, ymax =np.loadtxt(a, unpack=True)
            xmin=min(x)
            xmax=max(x)
            yerror=[ymin, ymax]
            adderrorbarnorm(x,y,yerror,label)
        elif type_g == 5:   #embeded normalized subplot
            x, y, ymin, ymax =np.loadtxt(a, unpack=True)
            xmin=min(x)
            xmax=max(x)
            yerror=[ymin, ymax]
            xs = x
            ys = y
            yserror = yerror
            embededsubploterrorbarnorm(ax1, ax2, x,y, yerror, label, xs, ys, yserror, label, i)
        elif type_g == 6:
            x, y, ymin, ymax =np.loadtxt(a, unpack=True)
            xmin=min(x)
            xmax=max(x)
            yerror=[ymin, ymax]
            #xs = x
            #ys = y
            #yserror = yerror
            #embededsubploterrorbarnorm2(ax1, ax2, x,y, yerror, label, xs, ys, yserror, label, i)
            embededsubploterrorbarnorm2(x,y, yerror, label, 2)
            
    if type_g == 5:
        #lgd=ax1.legend(bbox_to_anchor=(1.1,0.5), mode="expand", ncol=4, loc=3)
        lgd=ax1.legend(bbox_to_anchor=(0., 1.02, 1, .102), mode="expand", ncol=4, loc=3, borderaxespad=0.)
        #lgd=plt.legend(bbox_to_anchor=(0., 1.02, 1, .102), mode="expand", ncol=4, loc=3)
        ax1.set_xlabel(label_x)
        ax1.set_ylabel(label_y)
        #fig.xlabel(label_x)
        #fig.ylabel(label_y)
    else:
        #lgd=plt.legend(loc=0)
        lgd=plt.legend(bbox_to_anchor=(0., 1.02, 1, .102), mode="expand", ncol=4, loc=3, borderaxespad=0.)
        plt.xlabel(label_x)
        plt.ylabel(label_y)
    plt.xlim( 0.9*xmin, xmax+0.1*xmin )
    plt.title(title)
    if save:
        plt.savefig(save_in, bbox_extra_artists=(lgd,), bbox_inches='tight');
        #plt.savefig(save_in, bbox_extra_artists=(lgd));
    else:
        plt.show()

#f.text(0.5, 0.04, '# of Events', ha='center', va='center')
#f.text(0.04, 0.5, '# of Control Packets', ha='center', va='center', rotation='vertical')
#f.set_dpi(40)


#f.legend((p1,p2,p3),('SPT','DAARP','CER'), ncol=3, mode="expand", bbox_to_anchor=(0.15, 0.94, 0.7, 0.1), loc=3, borderaxespad=0., frameon=False)
#f.legend((p1,p2,p3,p4),('SPT','DAARP','CER-3','CER-1'), ncol=4, mode="expand", bbox_to_anchor=(0.15, 0.94, 0.7, 0.1), loc=3, borderaxespad=0., frameon=False)
#f.legend((p1,p2,p4),('SPT','DDAARP','CER'), ncol=4, mode="expand", bbox_to_anchor=(0.15, 0.94, 0.7, 0.1), loc=3, borderaxespad=0., frameon=False)
#f.legend(bbox_to_anchor=(0., 1.02, 1., 0.102), (p1,p2,p3),('SPT','DAARP','CERF'), ncol=3, mode="expand" )
#f.tight_layout()

#plt.savefig('AE.overhead4.png')

if __name__ == "__main__":
    main()
