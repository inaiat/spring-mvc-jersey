package hello.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public String execute() {
		try {
			Thread.sleep(200);
			return "Task finished";
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
	}

}