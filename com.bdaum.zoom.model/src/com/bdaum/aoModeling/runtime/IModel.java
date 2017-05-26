package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 * (c) 2002 Berthold Daum
 */
public interface IModel {

    /**
     * Adds a listener that listens to ValueChangedEvent. If the listener is
     * already registered this call has no effect.
     * 
     * @param listener -
     *            The listener
     */
    public void addValueChangedListener(ValueChangedListener listener);

    /**
     * Removes the listener if it exists.
     * 
     * @param listener -
     *            The listener
     */
    public void removeValueChangedListener(ValueChangedListener listener);

    /**
     * Sets the models command line parameters
     * 
     * @param args -
     *            the command line parameters
     */
    public void setParms(String[] args);

    /**
     * Retrieves the models command line parameters
     * 
     * @return - the command line parameters
     */
    public String[] getParms();

    /**
     * Enables global eventing. When global eventing is set, asset instances
     * created with this classes factory methods are added to the global event
     * listener.
     * 
     * @param globalEventing -
     *            true to enable global eventing
     */
    public void setGlobalEventing(boolean globalEventing);

    /**
     * Finds out if global eventing is switched on
     */
    public boolean getGlobalEventing();

    /**
     * Executes the model.
     */
    public void run();
}