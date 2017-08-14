package org.prototype.io;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import org.prototype.core.ClassAdvisor;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.Errors;
import org.prototype.reflect.MethodUtils;

@Component
public class FileMonitorClassAdvisor implements ClassAdvisor {

	private Timer timer;

	private Set<String> classes = new HashSet<>();

	@Override
	public void beforeLoad(ClassBuilder builder, Errors errors) {
		FileMonitor monitor = builder.getAnnotation(FileMonitor.class);
		if (monitor == null) {
			return;
		}
		classes.add(builder.getName());
	}

	@PreDestroy
	void destroy() {
		if (timer != null) {
			timer.cancel();
		}
	}

	private class TimerTaskImpl extends TimerTask {
		private FileMonitor monitor;
		private Object instance;
		private boolean updated;
		private boolean created;
		private boolean deleted;
		private FileStatus status;

		public TimerTaskImpl(FileMonitor monitor, Object instance) {
			scanFolder(new File(monitor.value()), monitor.deep());
		}

		@Override
		public void run() {
			try {
				if (updated) {
					checkedUpdated();
				}
				if (created) {
					checkedCreated();
				}
				if (deleted) {
					checkDeleted();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void checkedUpdated() throws Exception {
			if (status.folder) {
				// TODO
			} else {
				long t = status.file.lastModified();
				if (t != status.lastModify) {
					status.lastModify = t;
					onChanged(status.file, monitor.onUpdate());
				}
			}
		}

		private void onChanged(File file, String event) {
			Method method = MethodUtils.findMethod(instance.getClass(), monitor.onUpdate(), File.class);
			try {
				method.invoke(instance, file);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		}

		private void checkedCreated() throws Exception {
			if (!status.exists && status.file.exists()) {
				status.exists = true;
				status.lastModify = status.file.lastModified();
				onChanged(status.file, monitor.onCreate());
				if (status.folder) {
					// TODO
				}
			}
		}

		private void checkDeleted() throws Exception {
			if (status.exists && !status.file.exists()) {
				status.exists = false;
				onChanged(status.file, monitor.onDelete());
			}
		}

		private void scanFolder(File file, int index) {
			FileStatus status = new FileStatus();
			status.file = file;
			status.exists = file.exists();
			if (status.exists) {
				status.lastModify = file.lastModified();
			}
			if (file.isFile()) {

			}
		}

	}

	private class FileStatus {
		private boolean exists;
		private File file;
		private boolean folder;
		private long lastModify;
		private List<FileStatus> childs;
	}

	@Override
	public void onComplete(ClassFactory factory, Errors errors) {
		if (classes.isEmpty()) {
			return;
		}
		timer = new Timer("File monitor timer");
		for (String className : classes) {
			Class<?> clazz = factory.loadClass(className);
			try {
				TimerTaskImpl task = new TimerTaskImpl(clazz.getAnnotation(FileMonitor.class), clazz.newInstance());
				timer.schedule(task, task.monitor.period() * 1000);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
