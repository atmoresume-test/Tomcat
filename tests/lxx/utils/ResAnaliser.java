/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ResAnaliser {

    private static final String str1 = "| [[lxx.Tomcat]] 3.54a || [[Author]]      || Algorithm ||51,32 ||53,05 ||63,07 ||55,22 ||65,57 ||58,42 ||58,29 ||56,95 ||73,56 ||51,50 ||66,83 ||72,02 ||63,13 ||69,89 ||73,98 ||62,76 ||64,02 ||60,81 ||76,19 ||73,68 ||67,44 ||65,57 ||65,90 ||69,41 ||66,70 ||67,12 ||63,14 ||71,79 ||63,12 ||64,85 ||65,62 ||59,23 ||67,80 ||63,54 ||72,88 ||68,32 ||75,03 ||66,18 ||62,48 ||73,20 ||72,59 ||62,26 ||63,38 ||60,39 ||73,48 ||68,81 ||60,63 ||75,96 ||66,03 ||73,41 ||72,21 ||67,31 ||76,17 ||65,83 ||62,32 ||70,84 ||69,96 ||76,38 ||69,08 ||69,51 ||69,30 ||79,68 ||65,93 ||60,81 ||73,48 ||72,23 ||70,57 ||82,54 ||68,00 ||68,49 ||70,86 ||67,18 ||67,89 ||74,51 ||75,73 ||68,42 ||64,41 ||75,78 ||71,24 ||68,88 ||81,29 ||80,83 ||81,79 ||84,71 ||79,76 ||83,96 ||77,60 ||76,81 ||70,91 ||76,77 ||69,42 ||72,49 ||76,90 ||68,40 ||68,72 ||85,95 ||71,53 ||73,91 ||77,30 ||60,01 ||75,74 ||84,67 ||84,32 ||75,58 ||74,66 ||83,65 ||81,56 ||78,66 ||82,71 ||75,69 ||87,03 ||68,50 ||74,12 ||83,67 ||80,59 ||72,64 ||67,62 ||77,19 ||78,94 ||90,24 ||69,06 ||78,68 ||79,06 ||73,71 ||87,19 ||82,37 ||75,95 ||86,18 ||79,89 ||79,85 ||82,26 ||67,91 ||85,31 ||81,84 ||80,09 ||83,11 ||83,62 ||74,65 ||75,10 ||80,49 ||70,32 ||98,11 ||72,72 ||69,62 ||84,64 ||73,40 ||80,75 ||81,97 ||78,79 ||85,61 ||70,25 ||86,28 ||74,79 ||89,40 ||78,65 ||80,28 ||79,41 ||71,60 ||69,05 ||83,21 ||79,14 ||74,98 ||77,70 ||90,09 ||90,58 ||90,40 ||79,90 ||77,05 ||85,81 ||83,59 ||76,59 ||79,89 ||76,73 ||78,96 ||77,72 ||79,35 ||84,50 ||90,73 ||85,81 ||87,28 ||83,09 ||72,59 ||75,82 ||83,81 ||76,21 ||73,19 ||82,97 ||75,69 ||88,29 ||69,97 ||78,14 ||84,68 ||75,77 ||74,99 ||69,77 ||76,13 ||73,70 ||86,46 ||81,32 ||73,67 ||79,59 ||79,55 ||79,70 ||71,78 ||75,08 ||72,65 ||78,89 ||79,72 ||85,36 ||82,41 ||83,19 ||78,94 ||77,96 ||83,43 ||85,87 ||83,32 ||79,80 ||73,96 ||89,63 ||81,00 ||83,96 ||77,92 ||84,02 ||90,05 ||88,98 ||89,92 ||91,67 ||75,30 ||90,60 ||84,10 ||81,56 ||82,49 ||86,82 ||77,48 ||73,61 ||81,32 ||78,17 ||97,99 ||84,91 ||84,35 ||78,04 ||84,41 ||74,05 ||68,57 ||81,49 ||81,89 ||76,32 ||87,72 ||78,75 ||83,09 ||84,37 ||76,34 ||80,75 ||79,18 ||87,51 ||79,06 ||76,59 ||80,92 ||81,22 ||78,11 ||86,85 ||87,18 ||81,96 ||84,47 ||99,48 ||88,84 ||79,24 ||82,62 ||84,41 ||79,28 ||98,43 ||77,33 ||84,17 ||74,40 ||77,35 ||81,91 ||85,63 ||86,86 ||86,80 ||99,47 ||85,64 ||84,41 ||85,51 ||77,97 ||81,92 ||92,56 ||91,36 ||88,10 ||78,39 ||76,21 ||86,60 ||82,46 ||75,03 ||78,84 ||77,94 ||84,03 ||69,94 ||87,15 ||91,27 ||90,24 ||79,99 ||82,58 ||85,51 ||84,43 ||80,20 ||84,20 ||81,33 ||82,40 ||80,74 ||77,71 ||90,95 ||79,56 ||88,86 ||80,51 ||86,69 ||82,25 ||74,25 ||81,33 ||78,94 ||90,87 ||78,95 ||78,26 ||78,29 ||76,90 ||84,42 ||76,74 ||87,38 ||87,78 ||84,71 ||77,22 ||98,90 ||79,24 ||83,43 ||86,07 ||85,36 ||89,23 ||71,68 ||82,35 ||89,36 ||79,70 ||84,95 ||82,56 ||67,39 ||88,70 ||93,41 ||90,42 ||76,40 ||89,33 ||65,29 ||80,86 ||65,24 ||86,50 ||86,40 ||84,40 ||84,53 ||90,89 ||80,16 ||81,88 ||82,91 ||87,45 ||91,72 ||87,98 ||89,54 ||76,35 ||67,05 ||87,85 ||83,85 ||81,08 ||93,40 ||86,52 ||91,31 ||68,07 ||79,40 ||79,45 ||77,84 ||99,41 ||86,41 ||81,41 ||89,08 ||88,48 ||91,14 ||90,17 ||90,11 ||78,79 ||81,87 ||93,02 ||92,38 ||88,04 ||91,83 ||95,10 ||84,91 ||98,02 ||83,08 ||87,64 ||98,47 ||94,16 ||92,49 ||91,18 ||92,86 ||84,63 ||86,63 ||88,39 ||85,24 ||91,73 ||89,16 ||97,86 ||89,01 ||69,37 ||89,30 ||98,90 ||88,40 ||80,86 ||71,62 ||84,31 ||93,13 ||95,18 ||83,75 ||86,20 ||85,46 ||99,35 ||93,68 ||81,16 ||87,73 ||90,73 ||98,09 ||84,34 ||86,81 ||90,43 ||74,81 ||94,48 ||93,91 ||84,49 ||96,86 ||97,40 ||99,51 ||93,28 ||90,33 ||89,31 ||90,66 ||90,48 ||96,48 ||75,43 ||90,66 ||99,61 ||92,25 ||83,22 ||97,23 ||76,23 ||93,01 ||96,90 ||87,82 ||83,20 ||80,99 ||87,26 ||99,07 ||93,16 ||97,25 ||98,43 ||93,06 ||88,28 ||98,58 ||76,30 ||92,18 ||85,59 ||93,69 ||86,49 ||97,52 ||96,11 ||97,18 ||98,02 ||86,30 ||92,97 ||98,44 ||82,37 ||97,45 ||93,88 ||76,72 ||90,54 ||97,67 ||90,67 ||87,42 ||86,10 ||73,28 ||95,17 ||83,80 ||98,55 ||89,76 ||99,91 ||97,04 ||87,78 ||94,69 ||90,82 ||92,71 ||98,84 ||98,68 ||97,84 ||98,88 ||99,91 ||91,43 ||86,50 ||92,81 ||90,95 ||95,07 ||80,13 ||98,86 ||98,47 ||98,64 ||99,41 ||97,88 ||99,10 ||93,40 ||90,53 ||99,70 ||98,71 ||88,05 ||98,78 ||97,86 ||99,66 ||95,84 ||84,12 ||91,00 ||86,81 ||92,21 ||84,66 ||96,27 ||85,53 ||70,55 ||98,58 ||96,68 ||86,87 ||88,56 ||87,67 ||92,01 ||98,71 ||97,43 ||95,05 ||94,59 ||87,62 ||99,29 ||94,34 ||88,96 ||98,57 ||99,53 ||97,05 ||84,90 ||96,20 ||99,23 ||96,95 ||97,39 ||98,68 ||97,37 ||98,56 ||85,33 ||95,91 ||95,56 ||97,06 ||90,75 ||95,77 ||97,32 ||92,71 ||91,69 ||94,71 ||96,83 ||96,20 ||99,91 ||80,42 ||98,05 ||93,17 ||93,10 ||96,49 ||97,21 ||94,49 ||94,68 ||98,03 ||96,55 ||93,75 ||95,72 ||91,37 ||97,55 ||90,47 ||99,76 ||99,13 ||90,04 ||92,21 ||89,67 ||98,62 ||93,66 ||99,36 ||95,35 ||92,40 ||97,74 ||97,72 ||98,65 ||95,80 ||91,57 ||99,51 ||98,16 ||90,41 ||93,76 ||99,42 ||87,91 ||99,14 ||98,88 ||91,21 ||98,77 ||93,39 ||97,79 ||91,75 ||99,76 ||92,41 ||95,27 ||92,04 ||86,27 ||98,11 ||95,88 ||100,00 ||94,03 ||99,58 ||99,72 ||94,90 ||97,31 ||98,28 ||97,60 ||99,02 ||99,51 ||99,75 ||90,89 ||99,70 ||98,70 ||83,47 ||98,93 ||99,25 ||99,12 ||97,66 ||98,28 ||93,29 ||96,76 ||95,66 ||98,94 ||97,00 ||95,88 ||96,47 ||86,59 ||99,30 ||89,08 ||98,58 ||99,91 ||99,50 ||94,25 ||95,16 ||98,86 ||94,37 ||96,23 ||99,59 ||99,46 ||98,83 ||99,90 ||93,87 ||98,42 ||96,81 ||98,38 ||95,75 ||96,14 ||99,25 ||88,69 ||94,74 ||98,97 ||94,44 ||96,11 ||98,72 ||99,35 ||94,81 ||95,57 ||93,92 ||99,75 ||98,84 ||99,81 ||92,90 ||98,69 ||90,81 ||95,41 ||91,81 ||98,50 ||95,88 ||98,58 ||94,71 ||96,21 ||90,22 ||99,42 ||99,16 ||92,96 ||99,83 ||99,85 ||96,90 ||88,44 ||99,78 ||98,72 ||83,62 ||99,26 ||99,85 ||95,21 ||98,86 ||97,95 ||90,08 ||98,13 ||98,96 ||99,05 ||99,93 ||94,01 ||88,46 ||96,97 ||97,05 ||93,15 ||99,99 ||98,66 ||94,72 ||99,80 ||96,05 ||91,85 ||95,39 ||91,06 ||99,19 ||89,30 ||99,07 ||97,55 ||99,04 ||94,16 ||97,83 ||95,80 ||99,28 ||99,06 ||99,73 ||99,91 ||87,20 ||99,75 ||96,98 ||97,75 ||99,60 ||98,52 ||97,89 ||95,96 ||98,53 ||99,81 ||99,68 ||96,08 ||98,72 ||99,34 ||99,30 ||88,00 ||98,32 ||97,65 ||97,78 ||97,63 ||98,51 ||99,39 ||93,89 ||99,28 ||97,98 ||99,11 ||94,36 ||98,00 ||99,01 ||99,80 ||99,29 ||98,00 ||99,35 ||97,10 ||98,20 ||99,54 ||99,69 ||100,00 ||97,12 ||99,63 ||94,90 ||98,39 ||96,65 ||93,37 ||99,35 ||99,89 ||99,52 ||99,77 ||99,01 ||99,53 ||100,00 ||99,93 ||99,18 ||93,97 ||100,00 ||98,96 ||99,34 ||100,00 ||95,57 ||85,97 ||99,86 ||100,00 ||98,26 ||99,79 ||99,76 ||99,93 ||100,00 ||99,18 ||99,37 ||99,71 ||99,09 ||100,00 ||99,93 ||99,98 ||97,36 ||99,97 ||99,91 ||97,73 ||99,94 ||100,00 ||100,00 ||99,97 ||99,92 ||99,70 ||99,96 ||99,44 ||99,76 ||99,24 ||99,82 ||98,61 ||99,90 ||99,96 ||99,64 ||99,98 ||99,95 ||98,63 ||100,00 ||99,96 ||99,73 ||100,00 ||100,00 ||100,00 ||100,00 ||'''87,33 (89,13)''' ||'''87,33 (89,13)''' ||4 seasons";
    private static final String str2 = "| [[lxx.Tomcat]] 3.54a.mt.1 || [[Author]] || Algorithm ||52,31 ||52,13 ||68,55 ||54,13 ||73,37 ||49,13 ||55,17 ||56,78 ||73,75 ||65,50 ||71,05 ||70,02 ||63,14 ||65,06 ||51,63 ||62,00 ||61,79 ||66,01 ||71,55 ||67,28 ||66,70 ||56,34 ||69,66 ||59,64 ||56,21 ||70,52 ||67,08 ||62,87 ||64,13 ||69,21 ||63,67 ||65,49 ||63,70 ||69,73 ||68,07 ||62,13 ||74,80 ||62,59 ||62,17 ||65,96 ||70,32 ||63,82 ||68,17 ||61,68 ||74,30 ||68,41 ||64,14 ||74,62 ||53,04 ||72,54 ||70,42 ||67,91 ||82,28 ||66,64 ||72,38 ||68,30 ||73,08 ||65,57 ||69,47 ||70,14 ||73,33 ||76,27 ||67,83 ||63,20 ||71,65 ||64,16 ||71,34 ||80,39 ||61,37 ||57,36 ||71,13 ||71,36 ||66,98 ||74,68 ||76,09 ||69,19 ||69,62 ||69,52 ||78,21 ||72,73 ||71,03 ||78,02 ||80,57 ||87,64 ||79,18 ||81,03 ||79,52 ||72,83 ||76,61 ||79,64 ||76,71 ||70,15 ||78,94 ||52,55 ||74,50 ||85,25 ||68,27 ||71,18 ||68,15 ||63,64 ||75,06 ||83,29 ||87,66 ||76,22 ||71,56 ||88,11 ||86,11 ||80,74 ||83,71 ||74,62 ||81,93 ||68,06 ||76,17 ||82,41 ||80,04 ||78,85 ||68,47 ||75,56 ||73,07 ||80,45 ||67,62 ||73,19 ||83,82 ||78,59 ||83,89 ||90,21 ||76,10 ||84,26 ||84,97 ||71,52 ||81,46 ||69,34 ||89,33 ||75,66 ||82,67 ||80,31 ||76,02 ||73,28 ||75,70 ||70,17 ||68,14 ||97,71 ||71,80 ||72,14 ||83,14 ||75,66 ||84,45 ||76,34 ||74,47 ||87,24 ||74,18 ||90,41 ||67,82 ||91,17 ||76,42 ||83,98 ||78,77 ||70,88 ||63,74 ||79,25 ||73,17 ||80,40 ||80,98 ||89,65 ||89,59 ||91,02 ||74,13 ||81,39 ||82,69 ||78,28 ||82,27 ||86,62 ||75,63 ||75,93 ||82,79 ||78,90 ||81,21 ||91,76 ||86,66 ||85,30 ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||85,97 ||72,44 ||96,47 ||73,39 ||82,08 ||87,30 ||83,63 ||90,23 ||72,54 ||83,12 ||89,11 ||74,71 ||81,50 ||85,30 ||69,66 ||85,03 ||94,19 ||88,62 ||72,68 ||89,24 ||64,63 ||79,32 ||66,88 ||87,33 ||86,21 ||74,99 ||84,20 ||89,24 ||77,82 ||82,66 ||83,88 ||87,31 ||92,41 ||83,20 ||88,96 ||80,36 ||67,00 ||87,88 ||79,75 ||83,10 ||95,58 ||87,49 ||92,61 ||67,19 ||70,06 ||77,82 ||76,54 ||99,44 ||82,14 ||79,92 ||86,25 ||88,02 ||93,00 ||90,88 ||91,64 ||70,46 ||87,91 ||92,62 ||88,62 ||87,03 ||87,30 ||94,16 ||91,21 ||96,94 ||86,49 ||86,72 ||98,08 ||95,88 ||95,17 ||90,10 ||92,57 ||84,57 ||85,74 ||90,68 ||84,89 ||91,41 ||87,50 ||94,92 ||89,80 ||66,61 ||90,76 ||98,32 ||87,41 ||78,14 ||74,29 ||79,79 ||90,56 ||95,64 ||87,41 ||85,76 ||86,52 ||99,79 ||93,96 ||84,45 ||86,78 ||91,84 ||99,27 ||90,66 ||87,24 ||90,82 ||78,15 ||93,55 ||95,44 ||86,42 ||97,56 ||95,29 ||99,40 ||93,35 ||91,77 ||88,34 ||90,66 ||90,35 ||94,54 ||65,44 ||90,17 ||99,36 ||91,96 ||88,37 ||96,16 ||80,32 ||93,64 ||97,01 ||88,81 ||86,61 ||81,05 ||89,34 ||98,97 ||91,70 ||94,36 ||98,00 ||92,13 ||91,34 ||96,72 ||74,09 ||91,04 ||86,12 ||92,70 ||89,53 ||96,73 ||97,45 ||97,61 ||98,35 ||83,68 ||92,09 ||99,30 ||78,13 ||97,67 ||94,06 ||78,08 ||89,54 ||98,42 ||87,07 ||87,31 ||88,25 ||74,03 ||92,14 ||85,46 ||98,95 ||90,18 ||99,91 ||95,93 ||86,44 ||92,68 ||91,88 ||91,92 ||98,40 ||98,41 ||96,34 ||98,65 ||100,00 ||94,69 ||89,38 ||91,83 ||88,64 ||97,13 ||80,33 ||99,35 ||99,19 ||97,81 ||100,00 ||98,39 ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||96,53 ||99,64 ||92,61 ||98,28 ||98,62 ||98,04 ||96,79 ||96,69 ||99,19 ||94,01 ||96,71 ||99,75 ||94,64 ||97,84 ||98,90 ||99,31 ||96,92 ||95,71 ||95,12 ||99,77 ||99,45 ||99,98 ||89,70 ||99,06 ||89,67 ||96,66 ||91,58 ||97,67 ||95,41 ||98,05 ||94,76 ||96,35 ||90,32 ||98,83 ||99,31 ||93,34 ||99,79 ||99,85 ||97,07 ||88,82 ||99,78 ||97,83 ||81,33 ||99,04 ||99,49 ||96,20 ||99,32 ||99,37 ||88,01 ||97,89 ||99,16 ||99,68 ||99,93 ||94,38 ||86,83 ||96,69 ||98,93 ||93,84 ||100,00 ||99,57 ||95,59 ||100,00 ||97,09 ||94,64 ||96,14 ||92,08 ||99,83 ||86,64 ||99,04 ||97,48 ||99,09 ||96,04 ||98,77 ||95,31 ||99,83 ||98,03 ||99,64 ||99,30 ||85,76 ||99,94 ||97,65 ||97,15 ||99,79 ||97,65 ||98,58 ||95,41 ||98,20 ||99,98 ||99,39 ||97,40 ||99,00 ||99,95 ||99,19 ||99,86 ||98,91 ||97,62 ||99,39 ||98,68 ||97,55 ||98,85 ||95,67 ||98,70 ||98,10 ||98,65 ||96,17 ||96,46 ||99,70 ||99,84 ||99,68 ||97,93 ||99,26 ||96,57 ||99,76 ||99,14 ||100,00 ||100,00 ||97,27 ||99,83 ||96,36 ||97,34 ||96,46 ||93,91 ||99,93 ||100,00 ||99,72 ||100,00 ||98,12 ||99,39 ||100,00 ||99,71 ||99,70 ||94,78 ||100,00 ||98,97 ||99,23 ||98,16 ||94,66 ||86,53 ||99,98 ||99,98 ||99,64 ||99,17 ||99,69 ||100,00 ||99,98 ||98,00 ||99,44 ||99,67 ||99,14 ||100,00 ||99,98 ||100,00 ||100,00 ||99,86 ||99,93 ||98,99 ||100,00 ||100,00 ||100,00 ||99,93 ||99,97 ||99,86 ||100,00 ||98,85 ||100,00 ||100,00 ||99,77 ||99,00 ||99,98 ||100,00 ||99,60 ||99,54 ||100,00 ||99,66 ||99,98 ||100,00 ||99,01 ||100,00 ||100,00 ||100,00 ||100,00 ||'''86,50 (75,31)''' ||'''86,50 (75,31)''' ||4 seasons";

    public static void main(String[] args) {
        String[] str1Arr = str1.split("\\|\\|");
        String[] str2Arr = str2.split("\\|\\|");
        AvgValue diff = new AvgValue(10000);
        AvgValue avgAps1 = new AvgValue(10000);
        AvgValue avgAps2 = new AvgValue(10000);
        AvgValue avgDelta1 = new AvgValue(10000);
        AvgValue avgDelta2 = new AvgValue(10000);
        double minDiff = Integer.MAX_VALUE;
        double maxDiff = Integer.MIN_VALUE;
        Double prevAps1 = null;
        Double prevAps2 = null;
        int cnt = 0;
        for (int i = 0; i < min(str1Arr.length, str2Arr.length); i++) {
            try {
                final double aps1 = new Double(str1Arr[i].trim().replace(",", "."));
                final double aps2 = new Double(str2Arr[i].trim().replace(",", "."));
                if (Double.isNaN(aps1) || Double.isNaN(aps2)) {
                    continue;
                }
                avgAps1.addValue(aps1);
                avgAps2.addValue(aps2);
                final double d = aps2 - aps1;
                diff.addValue(d);
                minDiff = min(minDiff, d);
                maxDiff = max(maxDiff, d);
                if (prevAps1 != null) {
                    avgDelta1.addValue(aps1 - prevAps1);
                    avgDelta2.addValue(aps2 - prevAps2);
                }
                prevAps1 = aps1;
                prevAps2 = aps2;
                cnt++;
            } catch (NumberFormatException ignore) {
            }
        }

        System.out.println("Diff = " + diff.getCurrentValue());
        System.out.println("Ref APS = " + avgAps1.getCurrentValue());
        System.out.println("Chel aps = " + avgAps2.getCurrentValue());
        System.out.println("Wrost change = " + minDiff);
        System.out.println("best change =" + maxDiff);
        System.out.println("Cnt = " + cnt);
    }

}
