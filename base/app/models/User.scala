package models

import org.bson.types.ObjectId

case class User(login: String,
                password: String,
                _id: ObjectId = new ObjectId)
