import java.util.*;
import java.util.NoSuchElementException;

public class Main {
    public static MyLinkedList<Student> students = new MyLinkedList<>();
    static MyLinkedList<Course> courses = new MyLinkedList<>();
    static MyLinkedList<Grade> grades = new MyLinkedList<>();
    static RedBlackTree<Student> studentsTree;
    static RedBlackTree<Course> coursesTree;

    static Graph<Course> relativeGraph;
    static Graph<Student> comparisonGraph;

    static MyLinkedList<Integer> semesters;
    static CourseSemester[][] averageTable;
    static HashTable<Student> hashStudents;
    static HashTable<Course> hashCourses;


    static long a, b, p;

    public static void main(String[] args) {
//        File file = new File("./src/input.txt");
//        Scanner sc;
//        try {
//            sc = new Scanner(file);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        a = sc.nextLong();
        b = sc.nextLong();
        p = sc.nextLong();
        hashCourses = new HashTable<>(1, a, b, p);
        hashStudents = new HashTable<>(1, a, b, p);
        for (int i = 0; i < t; i++) {
            String state = sc.next();
            switch (state) {
                case "ADDS":
                    long id = sc.nextLong();
                    String name = sc.next();
                    Student student = new Student(id, name);
                    addToTree(student);
                    hashInsert(student);
                    break;
                case "ADDC":
                    Course course = new Course(sc.nextLong(), sc.next());
                    addToTree(course);
                    hashInsert(course);
                    break;
                case "ADDG":
                    Grade g = new Grade(sc.nextLong(), sc.nextLong(), sc.nextInt(), sc.nextFloat());
                    addGrade(g);
                    break;
                case "EDITS":
                    id = sc.nextLong();
                    name = sc.next();
                    student = Student.get(id);
                    if (student != null) {
                        deleteFromTree(student);
                        student.update(name);
                        addToTree(student);
                    }
                    break;
                case "EDITC":
                    id = sc.nextLong();
                    name = sc.next();
                    Course c = Course.get(id);
                    if (c != null) {
                        deleteFromTree(c);
                        c.update(name);
                        addToTree(c);
                    }
                    break;
                case "EDITG":
                    long studentId = sc.nextLong();
                    long courseId = sc.nextLong();
                    float grade = sc.nextFloat();
                    g = getGrade(studentId, courseId);
                    if (g != null)
                        g.update(grade);
                    break;
                case "DELETES":
                    id = sc.nextLong();
                    student = deleteStudent(id);
                    deleteFromTree(student);
                    hashDelete(student);
                    break;
                case "DELETEC":
                    id = sc.nextLong();
                    course = deleteCourse(id);
                    deleteFromTree(course);
                    hashDelete(course);
                    break;
                case "DELETEG":
                    studentId = sc.nextLong();
                    courseId = sc.nextLong();
                    student = Student.get(studentId);
                    course = Course.get(courseId);
                    if (student == null || course == null)
                        break;
                    student.removeGrade(courseId);
                    break;
                case "NUMBERC":
                    studentId = sc.nextLong();
                    int number = getNumberOfCourses(studentId);
                    System.out.println(number);
                    break;
                case "NUMBERS":
                    courseId = sc.nextLong();
                    number = getNumberOfStudents(courseId);
                    System.out.println(number);
                    break;
                case "SEARCHSN":
                    if (studentsTree == null) studentsTreePreprocess();
                    name = sc.next();
                    student = studentsTree.get(name);
                    if (student != null)
                        student.print();
                    break;
                case "SEARCHCN":
                    if (coursesTree == null) coursesTreePreprocess();
                    name = sc.next();
                    course = coursesTree.get(name);
                    if (course != null)
                        course.print();
                    break;
                case "SEARCHSC":
                    if (hashStudents == null) hashStudentsPreprocess();
                    id = sc.nextLong();
                    student = getStudentByKey(id);
                    if (student != null) {
                        System.out.println(hashStudents.hash(id));
                        student.print();
                    }
                    break;
                case "SEARCHCC":
                    if (hashCourses == null) hashCoursesPreprocess();
                    id = sc.nextLong();
                    course = getCourseByKey(id);
                    if (course != null) {
                        System.out.println(hashCourses.hash(id));
                        course.print();
                    }
                    break;
                case "ISRELATIVE":
                    if (relativeGraph == null) {
                        relativeGraphPreprocess();
                    }
                    long firstId = sc.nextLong();
                    long secondId = sc.nextLong();
                    boolean isRelative = relativeGraph.hasPath(firstId, secondId);
                    System.out.println(isRelative ? "yes" : "no");
                    break;
                case "ALLRELATIVE":
                    if (relativeGraph == null) relativeGraphPreprocess();
                    id = sc.nextLong();
                    printAllRelative(id);
                    break;
                case "COMPARE":
                    if (comparisonGraph == null) comparisonGraphPreprocess();
                    firstId = sc.nextLong();
                    secondId = sc.nextLong();
                    boolean firstToSecond = comparisonGraph.hasPath(firstId, secondId);
                    boolean secondToFirst = comparisonGraph.hasPath(secondId, firstId);
                    char ch = '?';
                    if (firstToSecond ^ secondToFirst) {
                        if (firstToSecond) ch = '>';
                        else ch = '<';
                    }
                    System.out.println(ch);
                    break;
                case "MINRISK":
                    if (semesters == null) minRiskInitialize();
                    studentId = sc.nextLong();
                    student = Student.get(studentId);
                    findMinRisk(student);
                    break;
                case "PRINTSL":
                    System.out.println(students);
                    break;
                case "PRINTCL":
                    System.out.println(courses);
                    break;
                case "PRINTST":
                    System.out.println(studentsTree);
                    break;
                case "PRINTCT":
                    System.out.println(coursesTree);
                    break;
                case "PRINTSG":
                    comparisonGraph.dfsPrint();
                    break;
                case "PRINTCG":
                    relativeGraph.dfsPrint();
                    break;
                default:
                    System.out.println("wrong input");

            }
        }
    }

    static float tempMaxScore = 0;
    static MyLinkedList<StudentSemester> tempBestPermutation;

    static void findMinRisk(Student student) {
        if (student == null) return;
        if (student.semesters == null) initStudentSemesters(student);
        Grade[] grades = new Grade[student.grades.size()];
        IIterator<Grade> iterator = student.getGradeIterator();
        int i = -1;
        while (iterator.hasNext()) {
            Grade grade = iterator.next();
            i++;
            grades[i] = grade;
        }
        tempBestPermutation = student.semesters;
        tempMaxScore = calculateScore(tempBestPermutation);
        findBestPermutation(grades.length, new MyLinkedList<>(), grades, student);
        printBestPermutation();
        tempBestPermutation = null;
        tempMaxScore = 0;
    }

    static void printBestPermutation() {
        MyLinkedList<StudentSemester> list = tempBestPermutation;
        IIterator<StudentSemester> iterator = list.iterator();
        while (iterator.hasNext()) {
            StudentSemester ss = iterator.next();
            System.out.print(ss.semester + " ");
            IIterator<Grade> it = ss.grades.iterator();
            while (it.hasNext()) {
                System.out.print(it.next().courseId + " ");
            }
            System.out.println();
        }
    }

    static void findBestPermutation(int n, MyLinkedList<Integer> permutation, Grade[] grades, Student student) {
        for (int i = 0; i < n; i++) {
            if (permutation.contains(i)) continue;
            permutation.add(i);
            findBestPermutation(n, permutation, grades, student);
            if (permutation.size() == n) {
                Grade[] grades1 = new Grade[n];
                int counter = -1;
                IIterator<Integer> iterator = permutation.iterator();
                while (iterator.hasNext()) {
                    int index = iterator.next();
                    counter++;
                    grades1[counter] = grades[index];
                }
                MyLinkedList<StudentSemester> list = getList(grades1, student);
                float score = calculateScore(list);
                if (score > tempMaxScore) {
                    tempMaxScore = score;
                    tempBestPermutation = list;
                    sort(list);
                } else if (score == tempMaxScore) {
                    sort(list);
                    tempBestPermutation = compareSortedPermutations(tempBestPermutation, list);
                }
            }
            permutation.remove(i);
        }
    }

    static MyLinkedList<StudentSemester> compareSortedPermutations(MyLinkedList<StudentSemester> list1, MyLinkedList<StudentSemester> list2) {
        IIterator<StudentSemester> it1 = list1.iterator();
        IIterator<StudentSemester> it2 = list2.iterator();
        while (it1.hasNext() || it2.hasNext()) {
            if (it1.hasNext() != it2.hasNext()) throw new RuntimeException("There is a bug here");
            StudentSemester ss1, ss2;
            ss1 = it1.next();
            ss2 = it2.next();
            if (ss1.semester != ss2.semester) throw new RuntimeException("There is a bug here");
            IIterator<Grade> git1, git2;
            git1 = ss1.grades.iterator();
            git2 = ss2.grades.iterator();
            while (git1.hasNext() || git2.hasNext()) {
                if (git1.hasNext() != git2.hasNext()) throw new RuntimeException("There is a bug here");
                Grade g1, g2;
                g1 = git1.next();
                g2 = git2.next();
//                int semesterIndex = semesters.indexOf(ss1.semester);
//                int c1Index, c2Index;
//                c1Index = courses.indexOf(g1.courseId);
//                c2Index = courses.indexOf(g2.courseId);
//                float score1, score2;
//                score1 = getScore(semesterIndex, c1Index);
//                score2 = getScore(semesterIndex, c2Index);

                if (g1.courseId < g2.courseId) {
                    return list1;
                } else if (g1.courseId > g2.courseId) return list2;
            }
        }
        return list1;
    }

    static void sort(MyLinkedList<StudentSemester> list) {
        list.sort(ssComparator);
        IIterator<StudentSemester> iterator = list.iterator();
        while (iterator.hasNext()) {
            StudentSemester ss = iterator.next();
            ss.grades.sort(Comparator.comparing(g -> g.courseId));
        }
    }

    static float getScore(int semesterIndex, int courseIndex) {
        return averageTable[semesterIndex][courseIndex].average;
    }

    static float calculateScore(MyLinkedList<StudentSemester> list) {
        float score = 0;
        IIterator<StudentSemester> iterator = list.iterator();
        while (iterator.hasNext()) {
            StudentSemester ss = iterator.next();
            IIterator<Grade> gIterator = ss.grades.iterator();
            while (gIterator.hasNext()) {
                Grade g = gIterator.next();
                int semesterIndex = semesters.indexOf(ss.semester);
                int courseIndex = courses.indexOf(g.courseId);
                score += getScore(semesterIndex, courseIndex);
            }
        }
        return score;
    }

    static MyLinkedList<StudentSemester> getList(Grade[] grades, Student student) {
        if (student == null) return null;
        MyLinkedList<StudentSemester> finalList = new MyLinkedList<>();
        int i = -1;
        IIterator<StudentSemester> iterator = student.semesters.iterator();
        while (iterator.hasNext()) {
            StudentSemester ss = iterator.next();
            StudentSemester fss = new StudentSemester(ss.semester);
            finalList.add(fss);
            IIterator<Grade> gradeIIterator = ss.grades.iterator();
            while (gradeIIterator.hasNext()) {
                gradeIIterator.next();
                i++;
                fss.grades.add(grades[i]);
                //todo if time limit occurred add(E element, Comparator<E>)
            }
        }
        return finalList;
    }

    static void initStudentSemesters(Student student) {
        if (student == null) return;
        student.semesters = new MyLinkedList<>();
        IIterator<Grade> iterator = student.getGradeIterator();
        while (iterator.hasNext()) {
            Grade grade = iterator.next();
            StudentSemester studentSemester = student.semesters.search(grade.semester);
            if (studentSemester == null) {
                studentSemester = new StudentSemester(grade.semester);
                student.semesters.add(studentSemester);
            }
            studentSemester.grades.add(grade);
        }
        sort(student.semesters);
    }

    static Comparator<StudentSemester> ssComparator = Comparator.comparing(o -> o.semester);


    static void minRiskInitialize() {
        findSemesters();
        createAverageTale();
    }

    static void createAverageTale() {
        int n = semesters.size();
        int m = courses.size();
        averageTable = new CourseSemester[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                averageTable[i][j] = new CourseSemester();
            }
        }
        IIterator<Course> courseIterator = courses.iterator();
        int courseIndex = -1;
        while (courseIterator.hasNext()) {
            Course course = courseIterator.next();
            courseIndex++;
            IIterator<Grade> gradeIterator = course.getGradeIterator();
            while (gradeIterator.hasNext()) {
                Grade grade = gradeIterator.next();
                int semester = grade.semester;
                int semIndex = semesters.indexOf(semester);
                averageTable[semIndex][courseIndex].addGrade(grade.grade);
            }
        }
    }

    static void findSemesters() {
        semesters = new MyLinkedList<>();
        IIterator<Grade> iterator = Grade.getIterator();
        while (iterator.hasNext()) {
            Grade grade = iterator.next();
            int semester = grade.semester;
            if (!semesters.contains(semester)) {
                semesters.add(semester);
            }
        }
    }

    static void printAllRelative(long id) {
        MyLinkedList<Course> list = relativeGraph.getAllRelative(id);
        long[] coursesId = new long[list.size()];
        IIterator<Course> iterator = list.iterator();
        int i = -1;
        while (iterator.hasNext()) {
            i++;
            coursesId[i] = iterator.next().id;
        }
        coursesId = mergeSort(coursesId);
        print(coursesId);
    }

    static void print(long[] longs) {
        StringBuilder stringBuilder = new StringBuilder();
        for (long l :
                longs) {
            stringBuilder.append(l).append(" ");
        }
        System.out.println(stringBuilder);
    }

    static long[] mergeSort(long[] longs) {
        int l = longs.length;
        if (l == 1 || l == 0) return longs;
        long[] first = new long[l / 2];
        long[] second = new long[l - l / 2];
        for (int i = 0; i < l; i++) {
            if (i < l / 2) first[i] = longs[i];
            else {
                second[i - l / 2] = longs[i];
            }
        }
        first = mergeSort(first);
        second = mergeSort(second);

        return merge(first, second);
    }

    static long[] merge(long[] first, long[] second) {
        int l1 = first.length;
        int l2 = second.length;
        long[] merged = new long[l1 + l2];
        int i = 0, j = 0, k = 0;
        while (i < l1 || j < l2) {
            if (i == l1) {
                merged[k] = second[j];
                k++;
                j++;
                continue;
            }
            if (j == l2) {
                merged[k] = first[i];
                k++;
                i++;
                continue;
            }
            if (first[i] > second[j]) {
                merged[k] = second[j];
                j++;
            } else {
                merged[k] = first[i];
                i++;
            }
            k++;
        }
        return merged;
    }

    static <E> Object[] mergeSort(Object[] objects, Comparator<E> comparator) {
        int l = objects.length;
        if (l == 1 || l == 0) return objects;
        Object[] first = new Object[l / 2];
        Object[] second = new Object[l - l / 2];
        for (int i = 0; i < l; i++) {
            if (i < l / 2) first[i] = objects[i];
            else {
                second[i - l / 2] = objects[i];
            }
        }
        first = mergeSort(first, comparator);
        second = mergeSort(second, comparator);

        return merge(first, second, comparator);
    }

    static <E> Object[] merge(Object[] first, Object[] second, Comparator<E> comparator) {
        int l1 = first.length;
        int l2 = second.length;
        Object[] merged = new Object[l1 + l2];
        int i = 0, j = 0, k = 0;
        while (i < l1 || j < l2) {
            if (i == l1) {
                merged[k] = second[j];
                k++;
                j++;
                continue;
            }
            if (j == l2) {
                merged[k] = first[i];
                k++;
                i++;
                continue;
            }
            if (comparator.compare((E) first[i], (E) second[j]) > 0) {
                merged[k] = second[j];
                j++;
            } else {
                merged[k] = first[i];
                i++;
            }
            k++;
        }
        return merged;
    }

    static void deleteFromTree(Student student) {
        if (studentsTree == null) return;
        studentsTree.delete(student);
    }

    static void deleteFromTree(Course course) {
        if (coursesTree == null) return;
        coursesTree.delete(course);
    }

    static void addToTree(Student student) {
        if (studentsTree == null) return;
        studentsTree.insert(student);
    }

    static void addToTree(Course course) {
        if (coursesTree == null) return;
        coursesTree.insert(course);
    }

    static void hashInsert(Student student) {
        if (hashStudents == null) return;
        if (hashStudents.size <= hashStudents.number) {
            HashTable<Student> newHashTable = new HashTable<>(2 * hashStudents.size, a, b, p);
            for (MyLinkedList<Student> l :
                    hashStudents.table) {
                if (l != null) {
                    IIterator<Student> iterator = l.iterator();
                    while (iterator.hasNext()) {
                        Student s = iterator.next();
                        newHashTable.insert(s, s.id);
                    }
                }
            }
            hashStudents = newHashTable;
        }
        hashStudents.insert(student, student.id);

    }

    static void hashInsert(Course course) {
        if (hashCourses == null) return;
        if (hashCourses.size <= hashCourses.number) {
            HashTable<Course> newHashTable = new HashTable<>(2 * hashCourses.size, a, b, p);
            for (MyLinkedList<Course> l :
                    hashCourses.table) {
                if (l != null) {
                    IIterator<Course> iterator = l.iterator();
                    while (iterator.hasNext()) {
                        Course c = iterator.next();
                        newHashTable.insert(c, c.id);
                    }
                }
            }
            hashCourses = newHashTable;
        }
        hashCourses.insert(course, course.id);
    }

    static void hashDelete(Student student) {
        if (hashStudents == null) return;
        if (hashStudents.number <= hashStudents.size / 4) {
            HashTable<Student> newHashTable = new HashTable<>(hashStudents.size / 2, a, b, p);
            for (MyLinkedList<Student> l :
                    hashStudents.table) {
                if (l != null) {
                    IIterator<Student> iterator = l.iterator();
                    while (iterator.hasNext()) {
                        Student s = iterator.next();
                        newHashTable.insert(s, s.id);
                    }
                }
            }
            hashStudents = newHashTable;
        }
        hashStudents.delete(student, student.id);
    }

    static void hashDelete(Course course) {
        if (hashCourses == null) return;
        if (hashCourses.number <= hashCourses.size / 4) {
            HashTable<Course> newHashTable = new HashTable<>(hashCourses.size / 2, a, b, p);
            for (MyLinkedList<Course> l :
                    hashCourses.table) {
                if (l != null) {
                    IIterator<Course> iterator = l.iterator();
                    while (iterator.hasNext()) {
                        Course c = iterator.next();
                        newHashTable.insert(c, c.id);
                    }
                }
            }
            hashCourses = newHashTable;
        }
        hashCourses.delete(course, course.id);
    }

    static void hashStudentsPreprocess() {
        hashStudents = new HashTable<>(1, a, b, p);
        IIterator<Student> iterator = students.iterator();
        while (iterator.hasNext()) {
            hashInsert(iterator.next());
        }
    }

    static void hashCoursesPreprocess() {
        hashCourses = new HashTable<>(1, a, b, p);
        IIterator<Course> iterator = courses.iterator();
        while (iterator.hasNext()) {
            hashInsert(iterator.next());
        }
    }

    static Student getStudentByKey(long key) {
        int hash = hashStudents.hash(key);
        MyLinkedList<Student> list = hashStudents.table[hash];
        if (list == null) return null;
        IIterator<Student> iterator = list.iterator();
        while (iterator.hasNext()) {
            Student student = iterator.next();
            if (student.id == key)
                return student;
        }
        return null;
    }

    static Course getCourseByKey(long key) {
        int hash = hashCourses.hash(key);
        MyLinkedList<Course> list = hashCourses.table[hash];
        if (list == null) return null;
        IIterator<Course> iterator = list.iterator();
        while (iterator.hasNext()) {
            Course course = iterator.next();
            if (course.id == key)
                return course;
        }
        return null;
    }


    static void studentsTreePreprocess() {
        studentsTree = new RedBlackTree<>();
        IIterator<Student> iterator = students.iterator();
        while (iterator.hasNext()) {
            studentsTree.insert(iterator.next());
        }
    }

    static void coursesTreePreprocess() {
        coursesTree = new RedBlackTree<>();
        IIterator<Course> iterator = courses.iterator();
        while (iterator.hasNext()) {
            coursesTree.insert(iterator.next());
        }
    }

    static Student deleteStudent(long studentId) {
        Student student = Student.remove(studentId);
        if (student == null) return null;
        IIterator<Grade> iterator = student.getGradeIterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        return student;
    }

    static Course deleteCourse(long courseId) {
        Course course = Course.remove(courseId);
        if (course == null) return null;
        IIterator<Grade> iterator = course.getGradeIterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        return course;
    }

    static Grade getGrade(long studentId, long courseId) {
        Student student = Student.get(studentId);
        if (student == null)
            return null;
        return student.getGrade(courseId);
    }

    static void addGrade(Grade grade) {
        Student student = Student.get(grade.studentId);
        Course course = Course.get(grade.courseId);
        if (student == null || course == null) return;
        MyLinkedList<Grade>[] lists = new MyLinkedList[3];
        lists[0] = grades;
        lists[1] = student.grades;
        lists[2] = course.grades;
        MyLinkedList.add(grade, lists);
    }

    static int getNumberOfCourses(long studentId) {
        Student student = Student.get(studentId);
        if (student == null) return -1;
        return student.grades.size();
    }

    static int getNumberOfStudents(long courseId) {
        Course course = Course.get(courseId);
        if (course == null) return -1;
        return course.grades.size();
    }

    static void relativeGraphPreprocess() {
        relativeGraph = new Graph<>(false);
        IIterator<Course> iterator = courses.iterator();
        while (iterator.hasNext()) {
            relativeGraph.addVertex(iterator.next());
        }
        iterator = courses.iterator();
        int i = -1;
        while (iterator.hasNext()) {
            Course course = iterator.next();
            i++;
            int j = -1;
            IIterator<Course> iterator1 = courses.iterator();
            while (iterator1.hasNext()) {
                Course course1 = iterator1.next();
                j++;
                if (j <= i) continue;
                if (course.isDirectRelative(course1)) {
                    relativeGraph.addEdge(course, course1);
                }
            }
        }
    }

    static void comparisonGraphPreprocess() {
        comparisonGraph = new Graph<>(true);
        IIterator<Student> iterator = students.iterator();
        while (iterator.hasNext()) {
            comparisonGraph.addVertex(iterator.next());
        }
        iterator = students.iterator();
        int i = -1;
        while (iterator.hasNext()) {
            Student student = iterator.next();
            i++;
            int j = -1;
            IIterator<Student> iterator1 = students.iterator();
            while (iterator1.hasNext()) {
                Student student1 = iterator1.next();
                j++;
                if (j <= i) continue;
                char ch = student.compare(student1);
                if (ch == '>') {
                    comparisonGraph.addEdge(student, student1);
                } else if (ch == '<')
                    comparisonGraph.addEdge(student1, student);
            }
        }
    }
}

class StudentSemester {
    int semester;

    int size() {
        return grades.size();
    }

    MyLinkedList<Grade> grades = new MyLinkedList<>();

    public StudentSemester(int semester) {
        this.semester = semester;
    }

    Comparator<Grade> ssgComparator = new Comparator<Grade>() {
        @Override
        public int compare(Grade o1, Grade o2) {
            int courseIndex1 = Main.courses.indexOf(o1.courseId);
            int courseIndex2 = Main.courses.indexOf(o2.courseId);
            if (courseIndex2 == -1) throw new NoSuchElementException("course2 not found");
            if (courseIndex1 == -1) throw new NoSuchElementException("course1 not found");
            int semesterIndex = Main.semesters.indexOf(semester);
            Float mean1 = Main.getScore(semesterIndex, courseIndex1);
            Float mean2 = Main.getScore(semesterIndex, courseIndex2);
            return mean1.compareTo(mean2);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (Objects.equals(semester, o)) return true;
        if (getClass() != o.getClass()) return false;
        StudentSemester that = (StudentSemester) o;
        return semester == that.semester;
    }
}

class Student implements Comparable<Object> {
    String name;
    long id;

    MyLinkedList<Grade> grades = new MyLinkedList<>();

    MyLinkedList<StudentSemester> semesters;

    public Student(long id, String name) {
        Main.students.add(this);
        this.name = name;
        this.id = id;
    }

    void update(String name) {
        this.name = name;
    }


    static Student get(long id) {
        IIterator<Student> iterator = Main.students.iterator();
        while (iterator.hasNext()) {
            Student s = iterator.next();
            if (s.id == id) return s;
        }
        return null;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + grades.size();
    }

    void print() {
        System.out.println(this);
        IIterator<Grade> iIterator = getGradeIterator();
        while (iIterator.hasNext()) {
            System.out.println(iIterator.next().toString(false));
        }
    }

    IIterator<Grade> getGradeIterator() {
        return grades.iterator(true, 1);
    }

    char compare(Student student) {
        int commonCoursesNumber = 0;
        int betterCourseNumber = 0;
        int worseCourseNumber = 0;
        IIterator<Grade> iterator = getGradeIterator();
        while (iterator.hasNext()) {
            Grade grade = iterator.next();
            IIterator<Grade> iterator1 = student.getGradeIterator();

            while (iterator1.hasNext()) {
                Grade g = iterator1.next();
                if (grade.courseId == g.courseId) {
                    commonCoursesNumber++;
                    if (grade.grade > g.grade) betterCourseNumber++;
                    else if (grade.grade < g.grade) worseCourseNumber++;
                    break;
                }
            }
        }
        char ch = '?';
        if (2 * betterCourseNumber > commonCoursesNumber) ch = '>';
        else if (2 * worseCourseNumber > commonCoursesNumber) ch = '<';
        return ch;
    }

    Grade getGrade(long courseId) {
        IIterator<Grade> iterator = this.getGradeIterator();
        while (iterator.hasNext()) {
            Grade g = iterator.next();
            if (g.courseId == courseId) {
                return g;
            }
        }
        return null;
    }

    void removeGrade(long courseId) {
        IIterator<Grade> iterator = this.getGradeIterator();
        while (iterator.hasNext()) {
            Grade g = iterator.next();
            if (g.courseId == courseId) {
                iterator.remove();
                return;
            }
        }
    }


    static Student remove(long id) {
        IIterator<Student> iterator = Main.students.iterator();
        while (iterator.hasNext()) {
            Student s = iterator.next();
            if (s.id == id) {
                iterator.remove();
                return s;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Student) {
            Student s = (Student) o;
            return this.name.compareTo(s.name);
        }
        if (o instanceof String) {
            String s = (String) o;
            return this.name.compareTo(s);
        }
        try {
            throw new Exception("error!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.name.equals(o) || Objects.equals(id, o)) return true;
        if (o.getClass() != this.getClass()) return false;
        Student student = (Student) o;
        return id == student.id && Objects.equals(name, student.name);
    }
}

class Course implements Comparable<Object> {
    long id;
    String name;

    MyLinkedList<Grade> grades = new MyLinkedList<>();

    public Course(long id, String name) {
        Main.courses.add(this);
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + grades.size();
    }

    void print() {
        System.out.println(this);
        IIterator<Grade> iIterator = this.getGradeIterator();
        while (iIterator.hasNext()) {
            System.out.println(iIterator.next().toString(true));
        }
    }

    void update(String name) {
        this.name = name;
    }

    Grade getGrade(long studentId) {
        IIterator<Grade> iterator = this.getGradeIterator();
        while (iterator.hasNext()) {
            Grade g = iterator.next();
            if (g.studentId == studentId) {
                return g;
            }
        }
        return null;
    }

    void removeGrade(long studentId) {
        IIterator<Grade> iterator = this.getGradeIterator();
        while (iterator.hasNext()) {
            Grade g = iterator.next();
            if (g.studentId == studentId) {
                iterator.remove();
                return;
            }
        }
    }

    IIterator<Grade> getGradeIterator() {
        return grades.iterator(true, 2);
    }

    boolean isDirectRelative(Course course) {
        int commonStudentsNumber = 0;
        IIterator<Grade> iterator = this.getGradeIterator();
        while (iterator.hasNext()) {
            Grade g = iterator.next();
            IIterator<Grade> iterator1 = course.getGradeIterator();
            while (iterator1.hasNext()) {
                if (g.studentId == iterator1.next().studentId) {
                    commonStudentsNumber++;
                    break;
                }
            }
        }
        return 2 * commonStudentsNumber > Math.max(this.grades.size(), course.grades.size());
    }

    static Course get(long id) {
        IIterator<Course> iterator = Main.courses.iterator();
        while (iterator.hasNext()) {
            Course c = iterator.next();
            if (c.id == id) return c;
        }
        return null;
    }


    static Course remove(long id) {
        IIterator<Course> iterator = Main.courses.iterator();
        while (iterator.hasNext()) {
            Course c = iterator.next();
            if (c.id == id) {
                iterator.remove();
                return c;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Course) {
            Course c = (Course) o;
            return this.name.compareTo(c.name);
        }
        if (o instanceof String) {
            String s = (String) o;
            return this.name.compareTo(s);
        }
        try {
            throw new Exception("error!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.name.equals(o) || Objects.equals(id, o)) return true;
        if (o.getClass() != this.getClass()) return false;
        Course course = (Course) o;
        return id == course.id && Objects.equals(name, course.name);
    }

}

class Grade {
    float grade;
    int semester;
    long studentId, courseId;

    @Override
    public String toString() {
        return "Grade{" +
                "grade=" + getGrade() +
                ", semester=" + semester +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                '}';
    }

    String getGrade() {
        int integer = (int) grade;
        return grade == integer ? String.valueOf(integer) : String.valueOf(grade);
    }

    public String toString(boolean hasStudent) {
        return (hasStudent ? studentId : courseId) + " " + semester + " " + getGrade();
    }


    public Grade(long studentId, long courseId, int semester, float grade) {
        this.grade = grade;
        this.semester = semester;
        this.courseId = courseId;
        this.studentId = studentId;
    }

    void update(float grade) {
        this.grade = grade;
    }

    static IIterator<Grade> getIterator() {
        return Main.grades.iterator(true, 0);
    }
}

interface IIterator<E> {
    boolean hasNext();

    E next();

    void remove();
}

interface IListIterator<E> extends IIterator<E> {
    boolean hasPrevious();

    E previous();

    void set(E element);
}

class LinkedListIterator<E> implements IListIterator<E> {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public E next() {
        return null;
    }

    @Override
    public void remove() {

    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public E previous() {
        return null;
    }

    @Override
    public void set(E element) {

    }
}


class MyLinkedList<E> {
    private Node<E> first;
    private Node<E> last;
    private int size = 0;

    int size() {
        return this.size;
    }

    public E getFirst() {
        if (first == null) throw new NoSuchElementException();
        return first.element;
    }

    public E getLast() {
        if (last == null) throw new NoSuchElementException();
        return last.element;
    }

    public MyLinkedList() {
    }

    public void add(E element) {
        Node<E> last = this.last;
        Node<E> node = new Node<>(element, last, null);
        this.last = node;
        if (last == null) {
            first = node;
        } else {
            last.next = node;
        }
        size++;
    }

    public static <E> void add(E element, MyLinkedList<E>[] lists) {
        int len = lists.length;
        Node<E> node = new Node<>(element, len);
        for (int i = 0; i < len; i++) {
            MyLinkedList<E> l = lists[i];
            if (l == null) continue;
            Node<E> last = l.last;
            node.prevNodes[i] = last;
            l.last = node;
            if (last == null) {
                l.first = node;
            } else last.nextNodes[i] = node;
            l.size++;
            node.lists[i] = l;
        }
    }

    public static <E> boolean remove(Node<E> node, boolean hasMultiPointer) {
        if (node == null) return false;
        if (!hasMultiPointer) throw new NoSuchElementException("raw use of method");
        for (int i = 0; i < node.pointersNumber; i++) {
            MyLinkedList<E> list = node.lists[i];
            if (list == null) continue;
            Node<E> prev = node.prevNodes[i];
            Node<E> next = node.nextNodes[i];

            if (prev != null) {
                prev.nextNodes[i] = next;
            } else {
                list.first = next;
            }
            if (next != null) {
                next.prevNodes[i] = prev;
            } else list.last = prev;
            list.size--;
        }
        node.clear();
        return true;
    }

    public E get(int index) {
        Node<E> node = getNode(index);
        return node.element;
    }

    private Node<E> getNode(int index) {
        checkIfIsOutOfBound(index);
        Node<E> node = this.first;
        index--;
        while (index >= 0) {
            node = node.next;
            index--;
        }
        return node;
    }

    private Node<E> getNodeByElement(Object object) {
        if (first.element.equals(object))
            return first;
        Node<E> node = first;
        while (node != null) {
            if (node.element.equals(object))
                return node;
            node = node.next;
        }
        return null;
    }

    public void set(E newElement, int index) {
        Node<E> node = getNode(index);
        node.element = newElement;
    }

    private void checkIfIsOutOfBound(int index) {
        if (index >= size || index < 0) throw new IndexOutOfBoundsException("Index " + index + " is out of bounds!");
    }


    public boolean remove(Object object) {
        Node<E> node = getNodeByElement(object);
        return removeNode(node);
    }

    void sort(Comparator<E> comparator) {
        int len = this.size;
        Object[] elements = new Object[len];
        int i = -1;
        IListIterator<E> iterator = this.iterator();
        while (iterator.hasNext()) {
            E element = iterator.next();
            i++;
            elements[i] = element;
        }
        elements = Main.mergeSort(elements, comparator);
        iterator = this.iterator();
        i = -1;
        while (iterator.hasNext()) {
            iterator.next();
            i++;
            iterator.set((E) elements[i]);
        }
    }


    public void clear() {
        for (Node<E> n = first; n != null; ) {
            Node<E> next = n.next;
            n.prev = null;
            n.next = null;
            n.element = null;
            n = next;
        }
        first = last = null;
        size = 0;
    }

    private boolean removeNode(Node<E> node) {
        if (node == null) return false;
        Node<E> prev = node.prev;
        Node<E> next = node.next;
        if (prev != null) {
            prev.next = next;
        } else {
            first = next;
        }
        if (next != null) {
            next.prev = prev;
        } else last = prev;
        node.clear();
        size--;
        return true;
    }

    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.element == null)
                    return index;
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.element.equals(o))
                    return index;
                index++;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.element == null)
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.element.equals(o))
                    return index;
            }
        }
        return -1;
    }

    public E search(Object o) {
        IIterator<E> iterator = this.iterator();
        while (iterator.hasNext()) {
            E element = iterator.next();
            if (element.equals(o)) {
                return element;
            }
        }
        return null;
    }

    int getSize() {
        IIterator<E> iterator = this.iterator();
        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next();
            counter++;
        }
        return counter;
    }

    public LinkedListIterator iterator() {
        return iterator(0);
    }

    public LinkedListIterator iterator(boolean isMultiPointer, int listIndex) {
        if (!isMultiPointer) throw new NoSuchElementException("raw use of function");
        return iterator(0, listIndex);
    }

    private LinkedListIterator iterator(int i) {
        if (i >= size) return new LinkedListIterator(null);
        Node<E> node = this.first;
        while (i > 0) {
            node = node.next;
            i--;
        }
        return new LinkedListIterator(node);
    }

    private LinkedListIterator iterator(int i, int listIndex) {
        if (i >= size) return new LinkedListIterator(null);
        Node<E> node = this.first;
        while (i > 0) {
            node = node.next;
            i--;
        }
        return new LinkedListIterator(node, listIndex);
    }

    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public String toString() {
        IIterator<E> iterator = iterator();
        if (!iterator.hasNext()) return "[]";
        StringBuilder stringBuilder = new StringBuilder("[");
        while (true) {
            stringBuilder.append(iterator.next());
            if (!iterator.hasNext()) {
                return stringBuilder.append("]").toString();
            }
            stringBuilder.append(", ");
        }
    }

    static class Node<E> {
        E element;
        Node<E> next;
        Node<E> prev;

        MyLinkedList<E>[] lists;
        Node<E>[] nextNodes;
        Node<E>[] prevNodes;
        int pointersNumber;

        public Node() {
        }

        public Node(E element) {
            this.element = element;
        }

        public Node(E element, Node<E> prev, Node<E> next) {
            this.element = element;
            this.next = next;
            this.prev = prev;
        }

        public Node(E element, int pointersNumber) {
            this.element = element;
            this.pointersNumber = pointersNumber;
            nextNodes = new Node[pointersNumber];
            prevNodes = new Node[pointersNumber];
            lists = new MyLinkedList[pointersNumber];
        }

        public void clear() {
            this.element = null;
            this.next = null;
            this.prev = null;
            this.nextNodes = null;
            this.prevNodes = null;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "element=" + element +
                    '}';
        }
    }

    private class LinkedListIterator implements IListIterator<E> {
        private Node<E> currentNode, next;

        final int listIndex;

        public LinkedListIterator(Node<E> next) {
            this.next = next;
            listIndex = -1;
            if (next != null) currentNode = next.prev;
        }

        public LinkedListIterator(Node<E> next, int listIndex) {
            this.next = next;
            this.listIndex = listIndex;
            if (next != null) currentNode = next.prevNodes[listIndex];
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }


        @Override
        public E next() {
            if (listIndex != -1) {
                return next(listIndex);
            }
            currentNode = next;
            next = next.next;
            return currentNode.element;
        }

        private E next(int listIndex) {
            currentNode = next;
            next = next.nextNodes[listIndex];

            return currentNode.element;
        }

        @Override
        public void remove() {
            if (currentNode == null) return;
            if (listIndex != -1) {
                remove(listIndex);
                return;
            }
            removeNode(currentNode);
        }

        private void remove(int listIndex) {
            MyLinkedList.remove(currentNode, true);
        }


        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public E previous() {
            return null;
        }

        @Override
        public void set(E element) {
            currentNode.element = element;
        }
    }
}

class RedBlackTree<E extends Comparable<Object>> {
    Node<E> root;
    Node<E> nullLeaf;
    int size = 0;

    public RedBlackTree() {
        nullLeaf = new Node<>(null, false);
        nullLeaf.right = nullLeaf.left = nullLeaf.parent = nullLeaf;
        root = nullLeaf;
    }

    void insert(E element) {
        size++;
        Node<E> node = this.root;
        Node<E> parent = nullLeaf;
        while (node != nullLeaf) {
            parent = node;
            if (element.compareTo(node.element) > 0) {
                node = node.right;
            } else if (element.compareTo(node.element) < 0) {
                node = node.left;
            } else throw new RuntimeException();
        }
        node = new Node<>(element, true, parent, nullLeaf, nullLeaf);
        if (parent == nullLeaf) {
            root = node;
            root.isRed = false;
            return;
        }
        if (element.compareTo(parent.element) > 0) {
            parent.right = node;
        } else parent.left = node;
        insertionFixup(node);
        resetNullLeaf();
    }

    void insertionFixup(Node<E> node) {
        Node<E> uncle;
        while (node != root && node.parent.isRed) {
            int i = node.parent == node.parent.parent.left ? 0 : 1;
            uncle = node.parent.parent.child(1 - i);
            if (uncle.isRed) {
                node.parent.isRed = false;
                uncle.isRed = false;
                node.parent.parent.isRed = true;
                node = node.parent.parent;
            } else {
                if (node == node.parent.child(1 - i)) {
                    node = node.parent;
                    rotate(node, i);
                }
                node.parent.isRed = false;
                node.parent.parent.isRed = true;
                rotate(node.parent.parent, 1 - i);
            }
        }
        root.isRed = false;
    }

    Node<E> successor(Node<E> node) {
        if (node.right != nullLeaf) return getMin(node.right);
        Node<E> p = node.parent;
        while (p != nullLeaf && p.right == node) {
            node = p;
            p = p.parent;
        }
        return p;
    }

    void resetNullLeaf() {
        nullLeaf.right = nullLeaf.left = nullLeaf.parent = nullLeaf;
        nullLeaf.isRed = false;
    }

    void delete(Object element) {
        Node<E> node = search(element);
        if (node == nullLeaf) {
            return;
        }
        size--;
        Node<E> temp;
        if (node.left == nullLeaf || node.right == nullLeaf) {
            temp = node;
        } else temp = getMin(node.right);
        Node<E> child = temp.left != nullLeaf ? temp.left : temp.right;
        child.parent = temp.parent;
        if (temp.parent == nullLeaf) {
            root = child;
        } else if (temp.parent.left == temp) temp.parent.left = child;
        else temp.parent.right = child;
        node.element = temp.element;
        if (!temp.isRed) {
            temp.left = temp.right = temp.parent = nullLeaf;
            temp.element = null;
            deleteFixup(child);
        }
        resetNullLeaf();
    }

    void deleteFixup(Node<E> node) {
        while (node != root && !node.isRed) {
            int i = node == node.parent.left ? 0 : 1;
            Node<E> brother = node.parent.child(1 - i);
            if (brother.isRed) {
                brother.isRed = false;
                node.parent.isRed = true;
                rotate(node.parent, i);
                brother = node.parent.child(1 - i);
            }
            if (!brother.left.isRed && !brother.right.isRed) {
                brother.isRed = true;
                node = node.parent;
            } else {
                if (!brother.child(1 - i).isRed) {
                    brother.child(i).isRed = false;
                    brother.isRed = true;
                    rotate(brother, 1 - i);
                    brother = node.parent.child(1 - i);
                }
                brother.isRed = node.parent.isRed;
                node.parent.isRed = false;
                brother.child(1 - i).isRed = false;
                rotate(node.parent, i);
                node = root;
            }
        }
        node.isRed = false;
    }


    Node<E> getMin(Node<E> node) {
        if (node == null || node == nullLeaf) return nullLeaf;
        if (node.left != nullLeaf) return getMin(node.left);
        return node;
    }

    private Node<E> search(Object element) {
        Node<E> node = root;
        while (node != nullLeaf) {
            int i = -node.element.compareTo(element);
            if (i > 0) node = node.right;
            else if (i < 0) node = node.left;
            else break;
        }
        return node;
    }

    E get(Object element) {
        Node<E> node = search(element);
        return node == null ? null : node.element;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    void rightRotate(Node<E> node) {
//        if (node.left == nullLeaf) throw new NoSuchElementException();
        Node<E> child = node.left;
        node.left = child.right;
        if (node.left != nullLeaf) node.left.parent = node;
        if (node.parent == nullLeaf) {
            root = child;
        } else if (node.parent.left == node) {
            node.parent.left = child;
        } else {
            node.parent.right = child;
        }
        child.parent = node.parent;
        child.right = node;
        node.parent = child;
    }

    void leftRotate(Node<E> node) {
//        if (node.right == nullLeaf) throw new NoSuchElementException();
        Node<E> child = node.right;
        node.right = child.left;
        if (node.right != nullLeaf) node.right.parent = node;
        if (node.parent == nullLeaf) {
            root = child;
        } else if (node.parent.right == node) {
            node.parent.right = child;
        } else {
            node.parent.left = child;
        }
        child.parent = node.parent;
        child.left = node;
        node.parent = child;
    }

    void rotate(Node<E> node, int i) {
        if (i == 0) {
            leftRotate(node);
        } else {
            rightRotate(node);
        }
    }

    void inOrderPrint() {
        Node.inOrderPrint(root);

    }


    static class Node<E extends Comparable<Object>> {
        E element;
        boolean isRed;
        Node<E> parent;
        Node<E> left;
        Node<E> right;

        Node<E> uncle() {
            if (parent == null || parent.parent == null) return null;
            if (parent == parent.parent.left)
                return parent.parent.right;
            return parent.parent.left;
        }

        public Node<E> child(int i) {
            return i == 0 ? left : right;
        }

        void clear() {
            this.left = null;
            this.right = null;
            this.parent = null;
            this.isRed = false;
            this.element = null;
        }

        public Node(E element, boolean isRed) {
            this.element = element;
            this.isRed = isRed;
        }


        public Node(E element, boolean isRed, Node<E> parent, Node<E> left, Node<E> right) {
            this.element = element;
            this.isRed = isRed;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            if (this.element == null) return "";
            StringBuilder buffer = new StringBuilder(50);
            print(buffer, "", "");
            return buffer.toString();
        }

        private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
            buffer.append(prefix);
            buffer.append(isRed ? "" : "*").append(element);
            buffer.append('\n');
            for (int i = 0; i < 2; i++) {
                Node<E> child = this.child(i);
                if (child == null || child.element == null) continue;
                if (i == 0) {
                    child.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
                } else {
                    child.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
                }
            }
        }

        static <E extends Comparable<Object>> void inOrderPrint(Node<E> node) {
            if (node == null || node.element == null) return;
            inOrderPrint(node.left);
            System.out.println((node.isRed ? "" : "*") + node.element);
            inOrderPrint(node.right);
        }
    }
}

class HashTable<E> {
    final int size;
    long a, b, p;

    int number = 0;


    final MyLinkedList<E>[] table;

    public HashTable(int size, long a, long b, long p) {
        this.size = size;
        table = new MyLinkedList[size];
        this.a = a;
        this.b = b;
        this.p = p;
    }

    int hash(long k) {
        return (int) (((a * k + b) % p) % size);
    }

    void insert(E element, long key) {
        int hash = hash(key);
        MyLinkedList<E> list = table[hash];
        if (list == null) {
            list = new MyLinkedList<>();
            table[hash] = list;
        }
        list.add(element);
        number++;
    }

    void delete(E element, long key) {
        int hash = hash(key);
        MyLinkedList<E> list = table[hash];
        if (list == null) {
            return;
        }
        if (list.remove(element)) {
            number--;
        }
    }

}

class Graph<E> {
    private int n = 0, m = 0;
    private final boolean isDirected;
    final private MyLinkedList<Vertex<E>> adjList = new MyLinkedList<>();

    public Graph(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public void addVertex(E element) {
        Vertex<E> vertex = new Vertex<>(element);
        adjList.add(vertex);
        n++;
    }

    public void addEdge(E from, E to) {
        Vertex<E> fromVertex = adjList.search(from);
        Vertex<E> toVertex = adjList.search(to);
        if (fromVertex == null || toVertex == null) throw new NoSuchElementException();
        fromVertex.addNeighbor(toVertex);
        if (!isDirected) {
            toVertex.addNeighbor(fromVertex);
        }
        m++;
    }

    boolean hasPath(Object from, Object to) {
        IIterator<Vertex<E>> iterator = adjList.iterator();
        int hasBoth = 0;
        Vertex<E> fromV = null, toV = null;
        while (iterator.hasNext()) {
            Vertex<E> next = iterator.next();
            if (next.equals(from)) {
                fromV = next;
                hasBoth++;
            } else if (next.equals(to)) {
                toV = next;
                hasBoth++;
            }
            if (hasBoth == 2) break;
        }
        if (hasBoth != 2) return false;
        dfsInit();
        boolean result = dfsHasPath(fromV, toV);
        dfsInit();
        return result;
    }


    private boolean dfsHasPath(Vertex<E> current, Vertex<E> destination) {
        if (current.color != Vertex.Color.WHITE)
            return false;
        current.color = Vertex.Color.GRAY;
        if (current.equals(destination)) return true;
        IIterator<Vertex<E>> iterator = current.neighbors.iterator();
        while (iterator.hasNext()) {
            Vertex<E> next = iterator.next();
            if (dfsHasPath(next, destination)) return true;
        }
        current.color = Vertex.Color.BLACK;
        return false;
    }

    int vertexNum() {
        return n;
    }

    int edgeNum() {
        return m;
    }

    boolean isDirected() {
        return isDirected;
    }

    MyLinkedList<E> getAllRelative(Object o) {
        MyLinkedList<E> list = new MyLinkedList<>();
        Vertex<E> vertex = adjList.search(o);
        if (vertex == null) return list;
        dfsInit();
        vertex.color = Vertex.Color.GRAY;
        IIterator<Vertex<E>> iterator = vertex.neighbors.iterator();
        while (iterator.hasNext()) {
            Vertex<E> next = iterator.next();
            dfsAddRelative(next, list);
        }
        dfsInit();
        return list;
    }

    private void dfsAddRelative(Vertex<E> vertex, MyLinkedList<E> list) {
        if (vertex.color != Vertex.Color.WHITE) return;
        vertex.color = Vertex.Color.GRAY;
        list.add(vertex.element);
        IIterator<Vertex<E>> iterator = vertex.neighbors.iterator();
        while (iterator.hasNext()) {
            dfsAddRelative(iterator.next(), list);
        }
        vertex.color = Vertex.Color.BLACK;
    }

    void dfsPrint() {
        dfsInit();
        IIterator<Vertex<E>> iterator = adjList.iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while (iterator.hasNext()) {
            Vertex<E> v = iterator.next();
            boolean isWhite = v.color == Vertex.Color.WHITE;
            if (isWhite) stringBuilder.append("{");
            dfsPrint(v, stringBuilder);
            if (isWhite) {
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                stringBuilder.append("}\n");
            }
        }
        System.out.print(stringBuilder);
        dfsInit();
    }


    private void dfsPrint(Vertex<E> vertex, StringBuilder stringBuilder) {
        if (vertex.color != Vertex.Color.WHITE)
            return;
        vertex.color = Vertex.Color.GRAY;
        stringBuilder.append(vertex.element).append(", ");
        IIterator<Vertex<E>> iterator = vertex.neighbors.iterator();
        while (iterator.hasNext()) {
            dfsPrint(iterator.next(), stringBuilder);
        }
        vertex.color = Vertex.Color.BLACK;
    }

    void dfsInit() {
        IIterator<Vertex<E>> iterator = adjList.iterator();
        while (iterator.hasNext()) {
            Vertex<E> v = iterator.next();
            v.resetColor();
        }
    }

    @Override
    public String toString() {
        return "Graph{" +
                "vertex size=" + n +
                ", edge size=" + m +
                ", type = " + (isDirected ? "" : "un") + "directed" +
                '}';
    }

    static class Vertex<E> {
        E element;
        MyLinkedList<Vertex<E>> neighbors = new MyLinkedList<>();

        Color color = Color.WHITE;

        public Vertex(E element) {
            this.element = element;

        }

        int degree() {
            return neighbors.size();
        }

        void resetColor() {
            this.color = Color.WHITE;
        }

        void addNeighbor(Vertex<E> neighbor) {
            neighbors.add(neighbor);
        }

        boolean removeNeighbor(Object neighbor) {
            return neighbors.remove(neighbor);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (Objects.equals(element, o)) return true;
            if (getClass() != o.getClass()) return false;
            Vertex<?> vertex = (Vertex<?>) o;
            return Objects.equals(element, vertex.element) && Objects.equals(neighbors, vertex.neighbors);
        }

        @Override
        public String toString() {
            return "Vertex{" +
                    "element=" + element +
                    '}';
        }

        enum Color {
            WHITE,
            GRAY,
            BLACK
        }
    }
}

class CourseSemester {
    int n = 0;
    float average = 0;

    void addGrade(float grade) {
        if (n == 0) {
            average = grade;
            n = 1;
            return;
        }
        float sum = n * average;
        sum += grade;
        n++;
        average = sum / n;
    }

    @Override
    public String toString() {
        return String.valueOf(average);
    }
}
