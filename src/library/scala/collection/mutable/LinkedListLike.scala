/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.collection
package mutable

import generic._
import annotation.tailrec

/** This extensible class may be used as a basis for implementing linked
 *  list. Type variable `A` refers to the element type of the
 *  list, type variable `This` is used to model self types of
 *  linked lists.
 *
 *  If the list is empty `next` must be set to `this`. The last node in every
 *  mutable linked list is empty.
 *
 *  Examples (`_` represents no value):
 *
 *  {{{
 *
 *     Empty:
 *
 *     [ _ ] --,
 *     [   ] <-`
 *
 *     Single element:
 *
 *     [ x ] --> [ _ ] --,
 *               [   ] <-`
 *
 *     More elements:
 *
 *     [ x ] --> [ y ] --> [ z ] --> [ _ ] --,
 *                                   [   ] <-`
 *
 *  }}}
 *
 *  @author  Matthias Zenger
 *  @author  Martin Odersky
 *  @version 1.0, 08/07/2003
 *  @since   2.8
 *
 *  @tparam A    type of the elements contained in the linked list
 *  @tparam This the type of the actual linked list holding the elements
 *
 *  @define Coll LinkedList
 *  @define coll linked list
 */
trait LinkedListLike[A, This <: Seq[A] with LinkedListLike[A, This]] extends SeqLike[A, This] { self =>

  var elem: A = _
  var next: This = _

  override def isEmpty = next eq this

  override def length: Int = length0(repr, 0)

  @tailrec private def length0(elem: This, acc: Int): Int = if (elem.isEmpty) acc else length0(elem.next, acc + 1)

  override def head: A    = elem

  override def tail: This = {
    require(nonEmpty, "tail of empty list")
    next
  }

  /** Append linked list `that` at current position of this linked list
   *  @return the list after append (this is the list itself if nonempty,
   *  or list `that` if list this is empty. )
   */
  def append(that: This): This = {
    @tailrec
    def loop(x: This) {
      if (x.next.isEmpty) x.next = that
      else loop(x.next)
    }
    if (isEmpty) that
    else { loop(repr); repr }
  }

  /** Insert linked list `that` at current position of this linked list
   *  @note this linked list must not be empty
   */
  def insert(that: This): Unit = {
    require(nonEmpty, "insert into empty list")
    if (that.nonEmpty) {
      next = next.append(that)
    }
  }

  override def drop(n: Int): This = {
    var i = 0
    var these: This = repr
    while (i < n && !these.isEmpty) {
      these = these.next.asInstanceOf[This] // !!! concrete overrides abstract problem
      i += 1
    }
    these
  }

  private def atLocation[T](n: Int)(f: This => T) = {
    val loc = drop(n)
    if (!loc.isEmpty) f(loc)
    else throw new IndexOutOfBoundsException(n.toString)
  }

  override def apply(n: Int): A   = atLocation(n)(_.elem)
  def update(n: Int, x: A): Unit  = atLocation(n)(_.elem = x)

  def get(n: Int): Option[A] = {
    val loc = drop(n)
    if (loc.nonEmpty) Some(loc.elem)
    else None
  }

  override def iterator: Iterator[A] = new Iterator[A] {
    var elems = self
    def hasNext = elems.nonEmpty
    def next = {
      val res = elems.elem
      elems = elems.next
      res
    }
  }

  override def foreach[B](f: A => B) {
    var these = this
    while (these.nonEmpty) {
      f(these.elem)
      these = these.next
    }
  }
}
