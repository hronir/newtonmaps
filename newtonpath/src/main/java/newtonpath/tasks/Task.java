package newtonpath.tasks;

import newtonpath.tasks.handler.ResultHandler;

public interface Task extends Runnable {

	public String getReference();

	public ResultHandler getResultHandler();

}