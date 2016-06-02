# Raymond
Implementation of Raymond's Mutual Exclusion algorithm

- Save Raymond.java file in a directory
- Open terminal and traverse to the directory.
- Compile the java file using javac *.java
- Run Raymond.java using java Raymond
- run this command on total seven servers namely: glados, kansas, newyork, arizona, missouri, california and nebraska

- initial configuration:
	glados (holder - glados), kansas (holder - glados), newyork (holder - glados), arizona (holder - kansas),	missouri (holder - kansas), 

california (holder - newyork) and nebraska (holder - glados)  

- glados has the token by default

Running example
- press 0 at nebraska to enter critical section from nebraska
- holder of glados changes to newyork
- holder of newyork holder changes to nebraska
- holder of nebraska holder changes to nebraska
- now nebraska after accessing critical section holds the token as there is no more requests in the queue
- press 0 at california to access critical section from california
- holder of nebraska holder changes to newyork
- holder of newyork holder changes to california
- now california after accessing critical section holds the token as there is no more requests in the queue
- press 0 at arizona to access critical section from arizona
- holder of california holder changes to newyork
- holder of newyork holder changes to glados
- holder of glados holder changes to kansas
- holder of kansas holder changes to arizona
- now arizona after accessing critical section holds the token as there is no more requests in the queue

To run on different servers you just need to change the IP wherever appropriate.
