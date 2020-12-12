package net.kardexo.ts3bot.util;

public class StringUtils
{
	public static String formatDuration(long gameDuration)
	{
		long seconds = gameDuration % 60;
		long minutes = (gameDuration % 3600) / 60;
		long hours = gameDuration / 3600;
		
		if(hours > 0)
		{
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		}
		
		return String.format("%02d:%02d", minutes, seconds);
	}
	
	public static String repeat(CharSequence sequence, int repetitions)
	{
		StringBuilder builder = new StringBuilder();
		
		for(int x = 0; x < repetitions; x++)
		{
			builder.append(sequence);
		}
		
		return builder.toString();
	}
	
	public static String emptyToNull(String string)
	{
		return string != null && !string.isEmpty() ? string : null;
	}
}
