#!/usr/bin/python

import sys, getopt, os
import matplotlib.pyplot as plt
import numpy as np

def adderrorbar(x, y, yerror, lname):
    plt.errorbar(x, y, yerr=yerror, label=lname)
    plt.grid(True)

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
    for a in args:
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

    plt.legend(loc=2)
    plt.xlabel(label_x)
    plt.ylabel(label_y)
    plt.xlim( 0.9*xmin, xmax+0.1*xmin )
    plt.title(title)
    if save:
        plt.savefig(save_in);
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
