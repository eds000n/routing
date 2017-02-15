package projects.GA.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects when a process is finished and invokes the associated listeners.
 * Adapted from https://beradrian.wordpress.com/2008/11/03/detecting-process-exit-in-java/
 */
public class ProcessExitDetector extends Thread {
 
    /** The process for which we have to detect the end. */
    private Process process;
    /** The associated listeners to be invoked at the end of the process. */
    private List<ProcessListener> listeners = new ArrayList<ProcessListener>();
 
    /**
     * Starts the detection for the given process
     * @param process the process for which we have to detect when it is finished
     */
    public ProcessExitDetector(Process process) {
        try {
            // test if the process is finished
            int exitValue = process.exitValue();
            
            BufferedReader sdterr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        	String l = null;
        	try {
				while((l=sdterr.readLine())!=null){
					System.out.println(l);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            throw new IllegalArgumentException("The process is already ended with value " +exitValue);
        } catch (IllegalThreadStateException exc) {
            this.process = process;
        }
    }
 
    /** @return the process that it is watched by this detector. */
    public Process getProcess() {
        return process;
    }
 
    public void run() {
        try {
            // wait for the process to finish
            process.waitFor();
            // invokes the listeners
            for (ProcessListener listener : listeners) {
                listener.processFinished(process);
            }
        } catch (InterruptedException e) {
        }
    }
 
    /** Adds a process listener.
     * @param listener the listener to be added
     */
    public void addProcessListener(ProcessListener listener) {
        listeners.add(listener);
    }
 
    /** Removes a process listener.
     * @param listener the listener to be removed
     */
    public void removeProcessListener(ProcessListener listener) {
        listeners.remove(listener);
    }
}