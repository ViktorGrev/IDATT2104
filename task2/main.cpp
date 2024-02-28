#include <iostream>
#include <thread>
#include <vector>
#include <queue>
#include <functional>
#include <mutex>
#include <condition_variable>
#include <chrono>

class Workers {
private:
    std::vector<std::thread> threads;
    std::queue<std::function<void()>> tasks;
    std::mutex queueMutex;
    std::condition_variable cv;
    bool stopRequested = false;

    void workerFunction() {
        while (true) {
            std::function<void()> task;
            {
                std::unique_lock<std::mutex> lock(queueMutex);

                // Wait until there is a task or stop is requested
                cv.wait(lock, [this] { return !tasks.empty() || stopRequested; });

                // If stop is requested and the task queue is empty, exit the loop
                if (stopRequested && tasks.empty()) {
                    break;
                }

                // Retrieve and remove the front task from the queue
                if (!tasks.empty()) {
                    task = tasks.front();
                    tasks.pop();
                }
            }

            // Execute the retrieved task
            if (task) {
                task();
            }
        }
    }

public:
    Workers(int numThreads) {
        for (int i = 0; i < numThreads; ++i) {
            // Create internal threads and associate them with the workerFunction
            threads.emplace_back(std::thread(&Workers::workerFunction, this));
        }
    }

    void start() {}

    // Post a task to the task queue
    void post(std::function<void()> task) {
        {
            std::lock_guard<std::mutex> lock(queueMutex);
            tasks.push(task);
        }
        // Notify one thread that a task is available
        cv.notify_one();
    }

    void post_timeout(std::function<void()> task, int milliseconds) {
        std::this_thread::sleep_for(std::chrono::milliseconds(milliseconds));
        post(task);
    }

    void stop() {
        {
            std::lock_guard<std::mutex> lock(queueMutex);
            stopRequested = true;
        }
        // Notify all threads to check the stop condition
        cv.notify_all();

        // Join all threads to wait for them to finish
        for (auto& thread : threads) {
            if (thread.joinable()) {
                thread.join();
            }
        }
    }

    ~Workers() {
        stop();
    }
};

int main() {
    Workers worker_threads(4);
    Workers event_loop(1);

    worker_threads.start();
    event_loop.start();

    worker_threads.post([] {
        std::cout << "Task A\n";
    });

    worker_threads.post([] {
        std::cout << "Task B\n";
    });

    event_loop.post([] {
        std::cout << "Task C\n";
    });

    event_loop.post([] {
        std::cout << "Task D\n";
    });

    // Post a task with a timeout for execution after 2000 milliseconds (2 seconds)
    event_loop.post_timeout([] {
        std::cout << "Task E (posted after timeout)\n";
    }, 2000);

    worker_threads.stop();
    event_loop.stop();

    return 0;
}
