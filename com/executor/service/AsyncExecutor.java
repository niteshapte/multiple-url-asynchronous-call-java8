package com.executor.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.TimeUnit;

/**
 * The Class AsyncExecutor.
 */
public class AsyncExecutor {
	
	/** The client. */
	JerseyRestClient client = JerseyRestClient.getInstance();

	/** The Constant urls. */
	private final static String[] urls = {"https://jsonplaceholder.typicode.com/posts", "https://jsonplaceholder.typicode.com/comments", "https://jsonplaceholder.typicode.com/albums", "https://jsonplaceholder.typicode.com/photos", "https://jsonplaceholder.typicode.com/todos", "https://jsonplaceholder.typicode.com/users"};
	
	/**
	 * Make async calls.
	 */
	public void makeAsyncCalls() {
		List<String> listOfURLs = Arrays.asList(urls);
		
		ExecutorServiceProvider<Void> executorServiceProvider = new ExecutorServiceProvider<Void>();
		ExecutorCompletionService<Void> executorCompletionService = new ExecutorCompletionService<Void>(executorServiceProvider.getExecutorService(5));
		
		listOfURLs.stream().filter(f -> f != null && !f.equals("")).forEach(url -> {
			Callable<Void> worker = () -> {
				callIndividualURLs(url);
				return null;
			};
			try {
				executorCompletionService.submit(worker);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		executorServiceProvider.getResult(listOfURLs.size(), executorCompletionService, 5, TimeUnit.SECONDS);
		executorServiceProvider.shutdownExecutorService();
	}
	
	/**
	 * Call individual UR ls.
	 *
	 * @param url the url
	 * @return the string
	 */
	public String callIndividualURLs(String url) {
		System.out.println(url);
		String data = client.getResponseAsString(url);
		System.out.println(data);
		return data;
	}
}
