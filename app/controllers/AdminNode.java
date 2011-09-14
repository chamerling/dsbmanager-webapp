package controllers;

import models.Node;
import play.mvc.With;

@CRUD.For(Node.class)
@With(Secure.class)
public class AdminNode extends CRUD {

}
