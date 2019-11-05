package shop.algebras

import cats.effect._
import cats.implicits._
import io.estatico.newtype.ops._
import org.scalatest.FunSuite
import shop.ItTestSuite
import shop.domain.auth._
import shop.domain.brand._
import shop.domain.category._
import shop.domain.cart._
import shop.domain.item._
import shop.domain.order._

class PostgreSQLTest extends ItTestSuite {

  spec("Brands") {
    sessionPool.use { pool =>
      LiveBrands.make[IO](pool).flatMap { b =>
        for {
          x <- b.findAll
          _ <- b.create("Foo".coerce[BrandName])
          y <- b.findAll
        } yield
          assert(
            x.isEmpty && y.exists(_.name.value == "Foo")
          )
      }
    }
  }

  spec("Categories") {
    sessionPool.use { pool =>
      LiveCategories.make[IO](pool).flatMap { c =>
        for {
          x <- c.findAll
          _ <- c.create("Foo".coerce[CategoryName])
          y <- c.findAll
        } yield
          assert(
            x.isEmpty && y.exists(_.name.value == "Foo")
          )
      }
    }
  }

  spec("Items") {
    def newItem(
        bid: Option[BrandId],
        cid: Option[CategoryId]
    ) = CreateItem(
      name = "item".coerce[ItemName],
      description = "desc".coerce[ItemDescription],
      price = USD(12),
      brandId = bid.getOrElse(randomId[BrandId]),
      categoryId = cid.getOrElse(randomId[CategoryId])
    )

    sessionPool.use { pool =>
      for {
        b <- LiveBrands.make[IO](pool)
        c <- LiveCategories.make[IO](pool)
        i <- LiveItems.make[IO](pool)
        x <- i.findAll
        _ <- b.create("bla".coerce[BrandName])
        d <- b.findAll.map(_.headOption.map(_.uuid))
        _ <- c.create("tzy".coerce[CategoryName])
        e <- c.findAll.map(_.headOption.map(_.uuid))
        _ <- i.create(newItem(d, e))
        y <- i.findAll
      } yield
        assert(
          x.isEmpty && y.exists(_.name.value == "item")
        )
    }
  }

  spec("Orders") {
    val orderId = randomId[OrderId]
    val userId  = randomId[UserId]
    //val paymentId = randomId[PaymentId]

    //val item = CartItem(
    //  Item(
    //    randomId[ItemId],
    //    "baz".coerce[ItemName],
    //    "foo".coerce[ItemDescription],
    //    USD(100),
    //    brand = Brand(randomId[BrandId], "Fender".coerce[BrandName]),
    //    category = Category(randomId[CategoryId], "Guitars".coerce[CategoryName])
    //  ),
    //  1.coerce[Quantity]
    //)

    sessionPool.use { pool =>
      LiveOrders.make[IO](pool).flatMap { o =>
        for {
          x <- o.findBy(userId)
          y <- o.get(userId, orderId)
          // TODO: See https://github.com/tpolecat/skunk/issues/83
          //i <- o.create(userId, paymentId, List(item), USD(100))
        } yield
          assert(
            x.isEmpty && y.isEmpty //&& i.value.toString.nonEmpty
          )
      }
    }
  }

}
