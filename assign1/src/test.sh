# Remove old binary
rm -f matrix 

# Compilation with warning, PAPI, optimization and OpenMP flags
g++ -Wall -O2 matrixproduct.cpp -o matrix -lpapi -fopenmp

# Run tests set for function 1
# echo "Function 1"
# for (( size = 600; size <= 3000; size += 400 )); do
#     for attempt in {1..4}; do
#         ./matrix "1" "$size" "data/data_cpp1_test.txt"
#     done
# done

# Run tests set for function 2
# echo "Function 2"
# for (( size = 600; size <= 3000; size += 400 )); do
#     for attempt in {1..4}; do
#         ./matrix "2" "$size" "data/data_cpp2_test.txt"
#     done
# done

# Run tests set for function 2 with larger matrices
# echo "Function 2 large"
# for (( size = 4096; size <= 10240; size += 2048 )); do
#     for attempt in {1..4}; do
#         ./matrix "2" "$size" "data/data_cpp2_large_test.txt"
#     done
# done

# Run tests set for function 3
# echo "Function 3"
# for (( size = 4096; size <= 10240; size += 2048 )); do
#     for block_size in 128 256 512; do
#         for attempt in 1 2 3 4; do
#             ./matrix "3" "$size" "$block_size" "data/data_cpp3_test.txt"
#         done
#     done
# done

# Run tests set for part 2
echo "Function 2 parallel 1"
for (( size = 600; size <= 3000; size += 400 )); do
    for attempt in {1..4}; do
        ./matrix "4" "$size" "data/data_cpp2_parallel1_test.txt"
    done
done

# Run tests set for part 2
echo "Function 2 parallel 2"
for (( size = 600; size <= 3000; size += 400 )); do
    for attempt in {1..4}; do
        ./matrix "5" "$size" "data/data_cpp2_parallel2_test.txt"
    done
done
