import java.util.*;

class clazz {
    public static void main(String[] args) {
        Graph<String> graph = new Graph<>(false);
        Scanner sc = new Scanner(System.in);
        MyLinkedList<MyString> strings = new MyLinkedList<>();
        loop:
        while (true) {
            String s = sc.next();
            switch (s) {
                case "av":
                    String v = sc.next();
                    graph.addVertex(v);
                    break;
                case "ae":
                    v = sc.next();
                    String v1 = sc.next();
                    graph.addEdge(v, v1);
                    break;
                case "dfs":
                    graph.dfsPrint();
                    break;
                case "end":
                    break loop;
                case "al":
                    int size = sc.nextInt();
                    for (int j = 0; j < size; j++) {
                        strings.add(new MyString(sc.next()));
                    }
                    System.out.println(strings);
                    break;
                case "l":
                    boolean a = sc.nextBoolean();
                    boolean b = sc.nextBoolean();
                    System.out.println(a ^ b);
                    break;
                case "s":
                    strings.sort(comparator);
                    System.out.println(strings);
                    break;
                case "clear":
                    strings.clear();
                    break;
            }
        }
    }

    static Comparator<MyString> comparator = Comparator.comparing(o -> o.string);
}

class another {
    public static void main(String[] args) {
        RedBlackTree<MyString> tree = new RedBlackTree<>();
        Scanner sc = new Scanner(System.in);
        while (true) {
            String s = sc.next();
            String i = sc.next();
            MyString m = new MyString(i);
            switch (s) {
                case "a":
                    tree.insert(m);
                    break;
                case "d":
                    tree.delete(m);
                    break;
                case "c":
                    System.out.println(tree.get(m));
                    break;
            }
        }
    }
}

class MyString implements Comparable<Object> {
    String string;

    @Override
    public String toString() {
        return string;
    }

    public MyString(String s) {
        this.string = s;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof String) return string.compareTo((String) o);
        if (o instanceof MyString) return string.compareTo(((MyString) o).string);
        throw new RuntimeException("CompareException");
    }
}

public class Test {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        int n = sc.nextInt();
        int[] maxes = new int[n];
        int[] current = new int[n];
        for (int i = 0; i < n; i++) {
            maxes[i] = sc.nextInt();
        }
        LinkedList<Integer> list = new LinkedList<>();
        printPermutationWithRepetition(t, n, maxes, list, current);
    }

    static int j = 0;

    static void printPermutation(int n, LinkedList<Integer> permutation) {
        printPermutation(n, n, permutation);
    }

    static void printPermutationWithRepetition(int totalNum, int groupNum, int[] groupsMaxSize, LinkedList<Integer> permutation, int[] gruopsCurrentSize) {
        for (int i = 0; i < groupNum; i++) {
            if (gruopsCurrentSize[i] == groupsMaxSize[i]) continue;
            permutation.add(i);
            gruopsCurrentSize[i]++;
            printPermutationWithRepetition(totalNum, groupNum, groupsMaxSize, permutation, gruopsCurrentSize);
            if (permutation.size() == totalNum) {
                j++;
                System.out.println("permutation " + j + ":" + permutation);
            }
            permutation.remove(Integer.valueOf(i));
            gruopsCurrentSize[i]--;
        }
    }

    static void printPermutation(int n, int r, LinkedList<Integer> permutation) {
        for (int i = 0; i < n; i++) {
            if (permutation.contains(i)) continue;
            permutation.add(i);
            printPermutation(n, r, permutation);
            if (permutation.size() == r) {
                j++;
                System.out.println("permutation " + j + ":" + permutation);
            }
            permutation.remove(Integer.valueOf(i));
        }
    }

}

class TestMultiPointerList {
    public static void main(String[] args) {
        MyLinkedList<MyString>[] lists = new MyLinkedList[2];
        MyLinkedList<MyString> list = new MyLinkedList<>();
        MyLinkedList<MyString> list1 = new MyLinkedList<>();
        MyLinkedList<MyString> list2 = new MyLinkedList<>();
        lists[0] = list;

        for (int i = 0; i < 10; i++) {
            lists[1] = i % 2 == 0 ? list1 : list2;
            MyLinkedList.add(new MyString(i + ""), lists);
        }

        IIterator<MyString> iterator = list.iterator(true, 1);
        int i = -1;
        while (iterator.hasNext()) {
            iterator.next();
            i++;
            if (i == i) iterator.remove();
        }
        System.out.println("remove done");
        iterator = list.iterator(true, 0);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        System.out.println("list size:" + list.size());

        iterator = list1.iterator(true, 1);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        System.out.println("list1 size:" + list1.size());

        iterator = list2.iterator(true, 1);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        System.out.println("list2 size:" + list2.size());


//        for (int i = 0; i < 10; i++) {
//            list.add(new MyString(i+""));
//        }
//        IIterator<MyString> iterator = list.iterator();
//        int i = -1;
//        while (iterator.hasNext()){
//            iterator.next();
//            i++;
//            if(i == 5) iterator.remove();
//        }
//        iterator = list.iterator();
//        while (iterator.hasNext()){
//            System.out.println(iterator.next());
//        }
    }
}