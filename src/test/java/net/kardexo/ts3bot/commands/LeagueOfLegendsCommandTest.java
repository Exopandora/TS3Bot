package net.kardexo.ts3bot.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.kardexo.ts3bot.api.LeagueOfLegends.League;
import net.kardexo.ts3bot.api.LeagueOfLegends.Rank;
import net.kardexo.ts3bot.api.LeagueOfLegends.Region;
import net.kardexo.ts3bot.api.LeagueOfLegends.RiotId;
import net.kardexo.ts3bot.api.LeagueOfLegends.Tier;
import net.kardexo.ts3bot.commands.impl.LeagueOfLegendsCommand;

class LeagueOfLegendsCommandTest
{
	@Test
	void testRiotIdParsing()
	{
		assertEquals(new RiotId("abc", "ABC"), RiotId.parse("abc#ABC", Region.EUW));
		assertEquals(new RiotId("abc", "FIVE5"), RiotId.parse("abc#FIVE5", Region.EUW));
		assertEquals(new RiotId("abc#SIX666", "EUW"), RiotId.parse("abc#SIX666", Region.EUW));
		assertEquals(new RiotId("abc#T2", "EUW"), RiotId.parse("abc#T2", Region.EUW));
		assertEquals(new RiotId("abc", "NA"), RiotId.parse("abc", Region.NA));
		assertEquals(new RiotId("#abc", "EUW"), RiotId.parse("#abc", Region.EUW));
		assertEquals(new RiotId("#abc", "NA"), RiotId.parse("#abc#NA", Region.NA));
		assertEquals(new RiotId("#abc#N", "EUW"), RiotId.parse("#abc#N", Region.EUW));
	}
	
	@Test
	void testLeagueToRating()
	{
		assertEquals(1, Tier.IRON.rating(Rank.IV));
		assertEquals(2, Tier.IRON.rating(Rank.III));
		assertEquals(3, Tier.IRON.rating(Rank.II));
		assertEquals(4, Tier.IRON.rating(Rank.I));
		assertEquals(5, Tier.BRONZE.rating(Rank.IV));
		assertEquals(6, Tier.BRONZE.rating(Rank.III));
		assertEquals(7, Tier.BRONZE.rating(Rank.II));
		assertEquals(8, Tier.BRONZE.rating(Rank.I));
		assertEquals(9, Tier.SILVER.rating(Rank.IV));
		assertEquals(10, Tier.SILVER.rating(Rank.III));
		assertEquals(11, Tier.SILVER.rating(Rank.II));
		assertEquals(12, Tier.SILVER.rating(Rank.I));
		assertEquals(13, Tier.GOLD.rating(Rank.IV));
		assertEquals(14, Tier.GOLD.rating(Rank.III));
		assertEquals(15, Tier.GOLD.rating(Rank.II));
		assertEquals(16, Tier.GOLD.rating(Rank.I));
		assertEquals(17, Tier.PLATINUM.rating(Rank.IV));
		assertEquals(18, Tier.PLATINUM.rating(Rank.III));
		assertEquals(19, Tier.PLATINUM.rating(Rank.II));
		assertEquals(20, Tier.PLATINUM.rating(Rank.I));
		assertEquals(21, Tier.EMERALD.rating(Rank.IV));
		assertEquals(22, Tier.EMERALD.rating(Rank.III));
		assertEquals(23, Tier.EMERALD.rating(Rank.II));
		assertEquals(24, Tier.EMERALD.rating(Rank.I));
		assertEquals(25, Tier.DIAMOND.rating(Rank.IV));
		assertEquals(26, Tier.DIAMOND.rating(Rank.III));
		assertEquals(27, Tier.DIAMOND.rating(Rank.II));
		assertEquals(28, Tier.DIAMOND.rating(Rank.I));
		assertEquals(29, Tier.MASTER.rating(Rank.IV));
		assertEquals(30, Tier.GRANDMASTER.rating(null));
		assertEquals(31, Tier.CHALLENGER.rating(null));
	}
	
	@Test
	void testRatingToLeague()
	{
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(1).get(), Tier.IRON, Rank.IV);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(2).get(), Tier.IRON, Rank.III);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(3).get(), Tier.IRON, Rank.II);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(4).get(), Tier.IRON, Rank.I);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(5).get(), Tier.BRONZE, Rank.IV);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(6).get(), Tier.BRONZE, Rank.III);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(7).get(), Tier.BRONZE, Rank.II);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(8).get(), Tier.BRONZE, Rank.I);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(9).get(), Tier.SILVER, Rank.IV);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(10).get(), Tier.SILVER, Rank.III);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(11).get(), Tier.SILVER, Rank.II);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(12).get(), Tier.SILVER, Rank.I);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(13).get(), Tier.GOLD, Rank.IV);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(14).get(), Tier.GOLD, Rank.III);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(15).get(), Tier.GOLD, Rank.II);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(16).get(), Tier.GOLD, Rank.I);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(17).get(), Tier.PLATINUM, Rank.IV);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(18).get(), Tier.PLATINUM, Rank.III);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(19).get(), Tier.PLATINUM, Rank.II);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(20).get(), Tier.PLATINUM, Rank.I);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(21).get(), Tier.EMERALD, Rank.IV);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(22).get(), Tier.EMERALD, Rank.III);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(23).get(), Tier.EMERALD, Rank.II);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(24).get(), Tier.EMERALD, Rank.I);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(25).get(), Tier.DIAMOND, Rank.IV);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(26).get(), Tier.DIAMOND, Rank.III);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(27).get(), Tier.DIAMOND, Rank.II);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(28).get(), Tier.DIAMOND, Rank.I);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(29).get(), Tier.MASTER, Rank.IV);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(30).get(), Tier.GRANDMASTER, null);
		assertLeague(LeagueOfLegendsCommand.ratingToLeague(31).get(), Tier.CHALLENGER, null);
	}
	
	private static void assertLeague(League actualLeague, Tier expectedTier, Rank expectedRank)
	{
		assertEquals(expectedTier, actualLeague.getTier());
		assertEquals(expectedRank, actualLeague.getRank());
	}
}
