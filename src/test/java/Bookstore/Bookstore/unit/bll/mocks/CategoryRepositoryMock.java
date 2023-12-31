package Bookstore.Bookstore.unit.bll.mocks;

import Bookstore.Bookstore.dal.models.Category;
import Bookstore.Bookstore.dal.repositories.irepositories.ICategoryRepository;

public class CategoryRepositoryMock extends RepositoryMock<Category> implements ICategoryRepository {
	@Override
	public Category getById(int id) {
		return dummyData.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
	}

	@Override
	public Category getByName(String name) {
		return dummyData.stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
	}

}
