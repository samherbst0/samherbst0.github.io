// CS-300
// Project Two
// Sam Herbst
// 10-20-24
// ProjectTwo.cpp

// Imports, specifically for vectors needed for course structure:
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <algorithm>

using namespace std;

// Create Course structure with number, title and prereqs: 
struct Course {
    string courseNumber;
    string courseTitle;
    vector<string> prerequisites;
};

vector<Course> courses;

// Create structure to load data, including error message:
void loadDataStructure(const string& fileName) {
    ifstream file(fileName);
    if (!file.is_open()) {
        cout << "Unable to open file: " << fileName << endl;
        return;
    }

    string line;
    while (getline(file, line)) {
        Course course;
        size_t pos = line.find(",");
        course.courseNumber = line.substr(0, pos);
        line.erase(0, pos + 1);
        pos = line.find(",");
        course.courseTitle = line.substr(0, pos);
        line.erase(0, pos + 1);

        while (!line.empty()) {
            pos = line.find(",");
            string prerequisite = line.substr(0, pos);
            course.prerequisites.push_back(prerequisite);
            if (pos == string::npos) break;
            line.erase(0, pos + 1);
        }

        courses.push_back(course);
    }

    file.close();
}

// Function to print course list:
void printCourseList() {
    sort(courses.begin(), courses.end(), [](const Course& a, const Course& b) {
        return a.courseNumber < b.courseNumber;
    });

    // Output: 
    cout << "Course List:" << endl;
    for (const auto& course : courses) {
        cout << course.courseNumber << ", " << course.courseTitle << endl;
    }
}

// Function to print individual course:
void printCourse(const string& courseNumber) {
    auto it = find_if(courses.begin(), courses.end(), [&](const Course& c) {
        return c.courseNumber == courseNumber;
    });

    // Prerequisite check:
    if (it != courses.end()) {
        cout << it->courseNumber << ", " << it->courseTitle << endl;
        cout << "Prerequisites: ";
        // No prerequisites:
        if (it->prerequisites.empty()) {
            cout << "None" << endl;
        } else {
            for (const auto& prerequisite : it->prerequisites) {
                cout << prerequisite << ", ";
            }
            cout << endl;
        }
    } else {
        // Course not found: 
        cout << "Course not found." << endl;
    }
}

// Build out menu:
int main() {
    string fileName;
    int choice;
    string courseNumber;

    // Menu title/welcome with options: 
    cout << "ABCU Comp Sci Course Planner" << endl;
    do {
        cout << "1. Load File Data" << endl;
        cout << "2. Print Course List" << endl;
        cout << "3. Print Course" << endl;
        cout << "9. Exit" << endl;
        cout << "Select option: ";
        cin >> choice;

        // Switcher:
        switch(choice) {
            // Load file:
            case 1:
                cout << "Enter file name: ";
                cin >> fileName;
                loadDataStructure(fileName);
                break;
            // Print list:
            case 2:
                printCourseList();
                break;
            // Print course:
            case 3:
                cout << "Select course for info: ";
                cin >> courseNumber;
                printCourse(courseNumber);
                break;
            // Exit:
            case 9:
                cout << "Exiting..." << endl;
                break;
            // Invalid:
            default:
                cout << choice << " is not valid." << endl;
        }
    } while (choice != 9);

    return 0;
}