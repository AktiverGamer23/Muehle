package de.htwg.se.muehle.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ObservableSpec extends AnyWordSpec with Matchers {

  "An Observable" when {
    class TestObservable extends Observable

    class TestObserver extends Observer {
      var updateCount = 0
      override def update: Unit = updateCount += 1
    }

    "created" should {
      val observable = new TestObservable

      "have empty subscribers" in {
        observable.subscribers should be(empty)
      }
    }

    "adding observers" should {
      "add single observer" in {
        val observable = new TestObservable
        val observer1 = new TestObserver

        observable.add(observer1)
        observable.subscribers should contain(observer1)
        observable.subscribers.length should be(1)
      }

      "add multiple observers" in {
        val observable = new TestObservable
        val observer1 = new TestObserver
        val observer2 = new TestObserver

        observable.add(observer1)
        observable.add(observer2)
        observable.subscribers should contain(observer1)
        observable.subscribers should contain(observer2)
        observable.subscribers.length should be(2)
      }

      "allow adding same observer multiple times" in {
        val observable = new TestObservable
        val obs = new TestObserver

        observable.add(obs)
        observable.add(obs)
        observable.subscribers.count(_ == obs) should be(2)
      }
    }

    "removing observers" should {
      val observable = new TestObservable
      val observer1 = new TestObserver
      val observer2 = new TestObserver
      val observer3 = new TestObserver

      observable.add(observer1)
      observable.add(observer2)
      observable.add(observer3)

      "remove single observer" in {
        observable.remove(observer1)
        observable.subscribers should not contain observer1
        observable.subscribers should contain(observer2)
        observable.subscribers should contain(observer3)
        observable.subscribers.length should be(2)
      }

      "remove multiple observers" in {
        observable.remove(observer2)
        observable.subscribers should not contain observer2
        observable.subscribers.length should be(1)
      }

      "handle removing non-existent observer" in {
        val nonExistent = new TestObserver
        observable.remove(nonExistent)
        observable.subscribers.length should be(1)
      }

      "remove all observers" in {
        observable.remove(observer3)
        observable.subscribers should be(empty)
      }
    }

    "notifying observers" should {
      val observable = new TestObservable
      val observer1 = new TestObserver
      val observer2 = new TestObserver
      val observer3 = new TestObserver

      observable.add(observer1)
      observable.add(observer2)
      observable.add(observer3)

      "notify all observers" in {
        observable.notifyObservers
        observer1.updateCount should be(1)
        observer2.updateCount should be(1)
        observer3.updateCount should be(1)
      }

      "notify multiple times" in {
        observable.notifyObservers
        observable.notifyObservers
        observer1.updateCount should be(3)
        observer2.updateCount should be(3)
        observer3.updateCount should be(3)
      }

      "not notify removed observer" in {
        observable.remove(observer2)
        observable.notifyObservers

        observer1.updateCount should be(4)
        observer2.updateCount should be(3)
        observer3.updateCount should be(4)
      }

      "work with empty subscribers" in {
        val emptyObservable = new TestObservable
        emptyObservable.notifyObservers
      }
    }

    "complex scenarios" should {
      "handle add and remove during notification" in {
        val observable = new TestObservable
        val observer = new TestObserver

        observable.add(observer)
        observable.notifyObservers
        observer.updateCount should be(1)

        observable.remove(observer)
        observable.notifyObservers
        observer.updateCount should be(1)
      }

      "maintain order of observers" in {
        val observable = new TestObservable
        val observers = (1 to 5).map(_ => new TestObserver).toVector

        observers.foreach(observable.add)

        observable.notifyObservers
        observers.foreach { obs =>
          obs.updateCount should be(1)
        }
      }

      "handle duplicate observers correctly" in {
        val observable = new TestObservable
        val observer = new TestObserver

        observable.add(observer)
        observable.add(observer)

        observable.notifyObservers
        observer.updateCount should be(2)

        observable.remove(observer)
        observable.notifyObservers
        observer.updateCount should be(2)
      }
    }

    "with custom observer implementation" should {
      class CountingObserver extends Observer {
        var count = 0
        override def update: Unit = count += 1
      }

      class LoggingObserver extends Observer {
        var log: List[String] = Nil
        override def update: Unit = log = "updated" :: log
      }

      "work with different observer types" in {
        val observable = new TestObservable
        val counter = new CountingObserver
        val logger = new LoggingObserver

        observable.add(counter)
        observable.add(logger)

        observable.notifyObservers

        counter.count should be(1)
        logger.log should be(List("updated"))

        observable.notifyObservers

        counter.count should be(2)
        logger.log should be(List("updated", "updated"))
      }
    }

    "mutability" should {
      "allow modification of subscribers" in {
        val observable = new TestObservable
        val observer1 = new TestObserver
        val observer2 = new TestObserver

        observable.add(observer1)
        val sizeAfterFirst = observable.subscribers.length

        observable.add(observer2)
        val sizeAfterSecond = observable.subscribers.length

        sizeAfterFirst should be(1)
        sizeAfterSecond should be(2)
      }
    }
  }
}
