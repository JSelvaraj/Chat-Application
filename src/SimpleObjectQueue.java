/**
  SimpleObjectQueue for use with MultiChatClient and MultiChatServer
  but could be used for other things, of course.

  Saleem Bhatti https://saleem.host.cs.st-andrews.ac.uk/
  September 2018
*/


public class SimpleObjectQueue {

  private Object[] queue_;

  private int size_ = 10; // minimum size
  private int head_ = 0;
  private int tail_ = 0;
  private int count_ = 0;
  private String id_ = "";

  SimpleObjectQueue(String id) {
    id_ = id;
    queue_ = new Object[size_];
  }

  SimpleObjectQueue(String id, int n) {
    id_ = id;
    if (n > 1) { size_ = n; }
    queue_ = new Object[size_];
  }

  public int size() { return (size_); }
  public int head() { return (head_); }
  public int tail() { return (tail_); }
  public int count() { return (count_); }
  public String id() { return (new String(id_)); }

  public boolean isEmpty() { return (count_ == 0); }
  public boolean isFull() { return (count_ == size_); }

  public Object get(int n) {
    return (n >= 0 && n < size_ ? queue_[n] : null);
  }

  public String print() {
    String s = new String(
      id_ + "->"
      + " head:" + head_
      + " tail:" + tail_
      + " count:" + count_
      );
    return s;
  } // print()

  public void add(Object o)
    throws QueueFullException {
    if (isFull()) { throw new QueueFullException(id_); }
    else {
      queue_[tail_] = o;
      ++count_;
      if (count_ == 1) { head_ = tail_; }
      tail_ = (tail_ + 1) % size_;
    }
  } // add()

  public Object remove()
    throws QueueEmptyException {
    if (isEmpty()) { throw new QueueEmptyException(id_); }
    else {
      Object o = queue_[head_];
      queue_[head_] = null;
      --count_;
      head_ = (head_ + 1) % size_;
      return (o);
    }
  } // remove()

  public void delete(Object o) { // overhead of holes in list
    if (o == null || count_ == 0) { return ; }

    int h;
    for (h = 0; h < size_; ++h) {
      if (queue_[h] == o) {
        queue_[h] = null; // a hole!
        break; // h will hold the index of the hole
      }
    }

    // holes in the queue are inconvenient, so:
    // - fill hole (tidy the queue_)
    // - update head_, tail_, and count_
    if (count_ > 1) {
      for (int n = h, p = n + 1; p != tail_; n = (n + 1) % size_, p = (n + 1) % size_) {
        if (queue_[n] == null) {
          // shuffle, fill in the hole
          queue_[n] = queue_[p]; // does not matter if queue_[p] is null
          queue_[p] = null;
        }
      }
      if (h == head_) { head_ = (head_ + 1) % size_; }
      if (h == tail_) { tail_ = tail_ > 0 ? tail_ - 1 : size_ - 1; }
      --count_;
    } // if (count_ > 1)
    else { head_ = tail_ = count_ = 0; }

  } // delete()

} // class SimpleObjectQueue
