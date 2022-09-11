# Solution for a Traffic Data Excercise

This repository presents my solution to an assessment excercise I did in 2022.
The [problem statement is available here](problem.md).


## Notes on the sample dataset

I have examined the sample input dataset (sample-data.json) to get a feeling for the data and to check for some common pitfalls. I have drawn some conclusions on what consitutes a valid dataset that I will explain below.

The sample dataset consists of 10 sets of measurements taken at different times. They are sorted in ascending order by the time they were taken. There are 30 streets and 20 avenues in the city described in the dataset, labeled 1 to 30 and A to T, respectively.

Each set contains measurements for the same pairs of intersections. 1150 of these are all the valid transit times between neighboring intersections (A2 -> A1 or A1 -> B1, for example; There is no measurement for A1 -> A2 as avenue A only allows northward traffic).
The remaining 7 measurements are a bit special. They make up the expressway.

The problem statement is not clear about whether one measurement must be for a direct transit between two intersections or if there could be measurements with several intersections "on the way". For example, there could be a measurement from 1A to 1C, which would consist of going from 1A to 1B and then form 1B to 1C. This would make estimating travel times more complicated. And in fact, the 7 measurements which supposedly make up the expressway could be interpreted as measurements with turns across the grid and multiple intersections inbetween. Looking at a distribution of the transit times beween direct neighbors, though, the 7 special fit right in with the other times. Because of that, and because all regular grid paths have exactly one measurement, I will conclude that any measurements between non-neighboring intersections are part of the expressway.


### Assumptions about the data:

- I assume that each set of measurements contains measurements between the same pairs of intersections. The programm will verify this.
- I assume that the grid is completely measured, i.e. that all valid paths between neighboring intersections are measured exactly once
  in each set of measurements. The program will verify this.
- I assume that each measurement means there is a direct path form the first intersection to the second.
- I will _not_ verify that there is only one expressway.


## Estimating transit times

There are multiple measurements for transit times for each road segment, taken at different times. There are several possible ways to create an estimate regarding the total travel time, depending on the use case and complexity:

- A simple solution is to just take the mean value of the measurements. If we have no other information about traffic patterns and want to
  output an average travel time to the user, this could be the way.
- We could just take the latest measurement. I have no information about how old the measurements are in relation to _now_, but the latest
  measurement contains the newest and therefore maybe the most accurate data.
- We could take the maximum of the measurements to give the user a conservative estimate how long it _could_ take to travel that route in
  the worst traffic situation.
- Similarly, we could take the minimum, if we wanted to give the user an estimate of how fast they could possibly get there.
- We could create a weighted average of the measurements, with newer data having heigher weight than older data. This would get us an estimate
  that includes more data than just taking the newest measurement but also emphasizes newer data, unlike the simple mean.

There is a number of other possibilities, of course. With enough measurement data, one could start to create a model of traffic patterns, maybe including daily variance or more sophisticated estimates. For the sake of simplicity, I will use the first method I suggested, the
simple arithmetic mean.


## Computing the fastest route

I will use Dijkstra's algorithm to find the shortest path between the starting and ending intersection. All path weights (transit times) are positive, otherwise I would have needed to use Bellman-Ford. I will not use A* because of the expressway; I think coming up with a heuristic that correctly accomodates the expressway is not worth the (probably nigligible) performance improvement.