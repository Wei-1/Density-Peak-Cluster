# ARodCluster
This project is the realization of the fast clustering method proposed by Alex Rodriguez.
Science 2014

## Note
The method is implemented by a n*n distance matrix, which is fast but will cause some memory problem if the amount of data is too big.

To solve the problem, you can change the algorithm by calculating density and delta separately without the distance matrix.
