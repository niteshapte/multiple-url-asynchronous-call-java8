package com.executor.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The Class ExecutorServiceProvider.
 *
 * @param <T> the generic type
 */
public class ExecutorServiceProvider<T> {

	/** The thread pool executor. */
	private ThreadPoolExecutor threadPoolExecutor;

	/**
	 * Gets the executor service.
	 *
	 * @return the executor service
	 */
	public ExecutorService getExecutorService() {
		return (null == threadPoolExecutor || threadPoolExecutor.isShutdown()) ? (ThreadPoolExecutor) Executors.newFixedThreadPool(5) : threadPoolExecutor;
	}

	/**
	 * Gets the executor service.
	 *
	 * @param threadCount the thread count
	 * @return the executor service
	 */
	public ExecutorService getExecutorService(int threadCount) {
		return (null == threadPoolExecutor || threadPoolExecutor.isShutdown()) ? (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount) : threadPoolExecutor;
	}

	/**
	 * Shutdown executor service.
	 */
	public void shutdownExecutorService() {
		if(null != threadPoolExecutor && !threadPoolExecutor.isShutdown()) {
			threadPoolExecutor.shutdown();
		}
	}

	/**
	 * Shutdown now executor service.
	 */
	public void shutdownNowExecutorService() {
		if(null != threadPoolExecutor && !threadPoolExecutor.isShutdown()) {
			threadPoolExecutor.shutdownNow();
		}
	}

	/**
	 * Gets the result.
	 *
	 * @param size the size
	 * @param executorCompletionService the executor completion service
	 * @param timeout the timeout
	 * @param timeUnit the time unit
	 * @return the result
	 */
	public Future<T> getResult(int size, final ExecutorCompletionService<T> executorCompletionService, final long timeout, final TimeUnit timeUnit) {
		Future<T> futureList = null;

		long globalWaitTime = timeUnit.toNanos(timeout);
		for(int i = 0; i < size; i++) {
			final long waitStart = System.nanoTime();

			try {
				futureList = executorCompletionService.take();
				if(futureList != null) {
					try {
						futureList.get(globalWaitTime, TimeUnit.NANOSECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						futureList.cancel(true);
					}
				}
			} catch (InterruptedException e) {
				futureList.cancel(true);
				Thread.currentThread().interrupt();
			} finally {
				final long waitFinish = System.nanoTime() - waitStart;
				globalWaitTime = Math.max(globalWaitTime - waitFinish, 0);
			}
		}
		return futureList;
	}
	
	/**
	 * Shutdown and await termination.
	 *
	 * @param pool the pool
	 */
	public void shutdownAndAwaitTermination(ExecutorService executorService) {
		executorService.shutdown();
	    try {
	        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
	        	executorService.shutdownNow();
	            if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
	                System.err.println("Pool did not terminate");
	        }
	    } catch (InterruptedException ie) {
	    	executorService.shutdownNow();
	        Thread.currentThread().interrupt();
	    }
	}
}
