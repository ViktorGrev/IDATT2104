#include <iostream>
#include <vector>
#include <thread>
#include <mutex>
#include <algorithm>

std::mutex printMutex;

void sieveOfEratosthenes(int start, int end, std::vector<bool>& isPrime) {
    for (int p = 2; p * p <= end; ++p) {
        if (isPrime[p]) {
            for (int i = p * p; i <= end; i += p)
                isPrime[i] = false;
        }
    }
}

void findPrimesInRange(int start, int end, const std::vector<bool>& isPrime, std::vector<int>& primes) {
    for (int i = std::max(2, start); i <= end; ++i) {
        if (isPrime[i]) {
            std::lock_guard<std::mutex> lock(printMutex);
            primes.push_back(i);
        }
    }
}

int main() {
    int startRange, endRange;
    std::cout << "Enter the start: ";
    std::cin >> startRange;
    std::cout << "Enter the end: ";
    std::cin >> endRange;

    std::vector<bool> isPrime(endRange + 1, true);

    // Apply the Sieve of Eratosthenes algorithm using 6 threads
    std::vector<std::thread> threads;
    for (int i = 0; i < 6; ++i) {
        threads.emplace_back(sieveOfEratosthenes, i + 2, endRange, std::ref(isPrime));
    }

    for (auto& thread : threads) {
        thread.join();
    }

    std::cout << "\nPrime numbers between " << startRange << " and " << endRange << " is:\n";

    // Find and print prime numbers in the given range
    threads.clear();
    std::vector<int> primes;
    for (int i = 0; i < 6; ++i) {
        threads.emplace_back(findPrimesInRange, startRange, endRange, std::cref(isPrime), std::ref(primes));
    }

    for (auto& thread : threads) {
        thread.join();
    }


    std::sort(primes.begin(), primes.end());
    primes.erase(std::unique(primes.begin(), primes.end()), primes.end());

    // Print the primes
    for (int prime : primes) {
        std::cout << prime << " ";
    }

    return 0;
}
