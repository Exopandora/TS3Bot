package net.kardexo.bot.services.api;

public interface IEconomyService
{
	void add(String user, long coins);
	
	void subtract(String user, long coins);
	
	long get(String user);
	
	boolean hasCoins(String user, long coins);
	
	void set(String user, long coins);
	
	boolean transfer(String sender, String beneficiary, long coins);
	
	String getCurrency();
}
