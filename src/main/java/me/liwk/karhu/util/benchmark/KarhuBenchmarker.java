package me.liwk.karhu.util.benchmark;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KarhuBenchmarker {
   private static final Map profiles = new HashMap();

   public static void registerProfiles() {
      BenchmarkType[] var0 = BenchmarkType.values();
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         BenchmarkType profileType = var0[var2];
         profiles.put(profileType, new Benchmark(profileType, profileType.precision()));
      }

   }

   public static Benchmark getProfileData(BenchmarkType profileType) {
      return (Benchmark)profiles.get(profileType);
   }

   public static List sortedProfiles() {
      List sorted = (List)profiles.values().stream().sorted(Comparator.comparingDouble(Benchmark::runningAverage)).collect(Collectors.toList());
      Collections.reverse(sorted);
      return sorted;
   }
}
