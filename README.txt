==== 
  TRAP 13 
==== 

Trap 13 is a single player card game which you can either win or lose.



==== 
  THE GAME 
==== 

To begin the player must sort a deck of 52 cards into the 4 suits. (there should be 13 cards in each suit).
Each suit is shuffled and placed on the table face down.
The top card of each suit is turned face up.

eg [ A, 3, K, 3] 

To win the player must get all the top cards to same number

eg [ K, K, K, K] wins 
   [ K, K, K, Q] does not win (even though K haS the same value as Q.

To change the top card of a deck the player turns the top card face down and removes n cards from the top to the bottom of the pile, where n is the value of the top card that was turned over initially.

eg if the top card is a 3 then to make a move on that pile the player must remove 3 cards from the top of the pile to the bottom of the pile. (These 3 cards will consist of the 3 the 3 that was turned over initially followed by 2 more cards).

It is important to retain the order when transferring cards from the top to the bottom.
Order is retained if moving (eg) 3 cards 1 by one, OR by moving all 3 cards at once.

All picture cards are worth 10. (moving 10 cards to the bottom is equivelent to moving 3 cards from the bottom to the top).
Aces are worth 1.

==== 
  GAME ANALYSIS 
==== 

Consider the following starting configuration for a shuffled suit:

  [ A, 3, 2, 4, 10, 5, 6, 7, 8, 9, J, Q, K]

Following the iterative process of augmenting the top card yields the sequence:

  A -> 3 -> 10 -> 3 -> 10 -> 3 etc....

Pre Sequence: [ A] 
Loop Sequence: [ 3, 10]

Conclusion: Pre Sequence cards can only be achieved once.
    Loop Sequence cards will always be available at any point in the game.
    Depending on the configuration; many cards will be unachievable.


If the above sequence is the first pile: 
   consider the following configuration for the second pile:

  [ 4, 2, 3, 5, 8, 6, 7, 9, 10, J, Q, K, A]

  4 -> 8 -> A -> 4....

Pre Sequence: null 
Loop Sequence: [ 4, 8, A]

The only common card in these two sequences is the Ace.
If the first pile is augmented first then the ace will become unachievable and therefore the game will be impossible to win.
However if the second pile is augmented first then ot is possible to achieve twp aces.

=== Chance of winning 

== Lower Bound 

since all the cards in the loop sequence will always be available the lower bound is defined by the number of permutations with 4 common cards in the loop sequence only.

== Upper Bound 

the upper bound is defined by the number of permutations that contain 4 comman cards in the pre sequence and loop sequence combined.
this combination or pre and loop defines the maximum possible reach for that that starting configuratipn.

nb. no algorithm will be able to reach this upper bound consistently as it requires knowledge of the configuration order.
however this will still define the upper bound as it is possible to reach all of these cards.

== Permutations 

there are 13 cards in each pile: 13! perms 
there are 4 piles: 13!^4 perms 

= ~1.5E+39




==== 
  PermCounter.java 
==== 

//TODO
