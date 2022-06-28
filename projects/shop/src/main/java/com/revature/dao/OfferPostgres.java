package com.revature.dao;

import com.revature.model.Item;
import com.revature.model.Offer;
import com.revature.model.User;
import com.revature.util.ConnectionUtil;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class OfferPostgres implements Dao<Offer> {

    private String schema = "public";

    public OfferPostgres() {
        super();
    }

    public OfferPostgres(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public Offer get(int userId) {
        Offer offer = null;
        String sql = "SELECT * FROM offer where user_id=?;";

        try (Connection c = ConnectionUtil.getConnectionFromFile()) {
            c.setSchema(schema);
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int itemId = rs.getInt("item_id");
                LocalDate offerDate = rs.getDate("offer_date").toLocalDate();
                float offerAmount = rs.getFloat("offer_amount");

                User user = new UserPostgres(schema).get(userId);
                Item item = new ItemPostgres(schema).get(itemId);
                offer = new Offer(user, item, offerDate, offerAmount);
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return offer;
    }

    public Offer get(int userId, int itemId) {
        Offer offer = null;
        String sql = "SELECT * FROM offer where user_id=? AND item_id=?;";

        try (Connection c = ConnectionUtil.getConnectionFromFile()) {
            c.setSchema(schema);
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, itemId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LocalDate offerDate = rs.getDate("offer_date").toLocalDate();
                float offerAmount = rs.getFloat("offer_amount");

                User user = new UserPostgres(schema).get(userId);
                Item item = new ItemPostgres(schema).get(itemId);
                offer = new Offer(user, item, offerDate, offerAmount);
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return offer;
    }

    @Override
    public List<Offer> getAll() {
        List<Offer> offers = new ArrayList<>();
        String sql = "SELECT * FROM offer";

        try (Connection c = ConnectionUtil.getConnectionFromFile()) {
            c.setSchema(schema);
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int itemId = rs.getInt("item_id");
                LocalDate offerDate = rs.getDate("offer_date").toLocalDate();
                float offerAmount = rs.getFloat("offer_amount");

                User user = new UserPostgres(schema).get(userId);
                Item item = new ItemPostgres(schema).get(itemId);
                Offer offer = new Offer(user, item, offerDate, offerAmount);
                offers.add(offer);
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }

        return offers;
    }

    @Override
    public Offer insert(Offer offer) {
        String sql = """
                INSERT INTO offer (user_id, item_id, offer_amount, offer_date)
                VALUES (?, ?, money(?::numeric), ?)
                returning user_id, item_id;
                """;

        try (Connection c = ConnectionUtil.getConnectionFromFile()) {
            c.setSchema(schema);
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, offer.getUser().getId());
            ps.setInt(2, offer.getItem().getId());
            ps.setDouble(3, offer.getAmount());
            ps.setDate(4, Date.valueOf(offer.getDate()));

            ResultSet rs = ps.executeQuery(); // return the id generated by the database
            if (rs.next()) {
                offer.getUser().setId(rs.getInt("user_id"));
                offer.getItem().setId(rs.getInt("item_id"));
            }

        } catch (SQLException | IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

        return offer;
    }

    @Override
    public boolean update(Offer offer) {
        String sql = "UPDATE offer SET offer_date = ?, offer_amount = money(?::numeric) WHERE user_id = ? AND item_id = ?;";

        int rowsChanged = -1;

        try (Connection c = ConnectionUtil.getConnectionFromFile()) {
            c.setSchema(schema);
            PreparedStatement ps = c.prepareStatement(sql);

            ps.setDate(1, Date.valueOf(offer.getDate()));
            ps.setDouble(2, offer.getAmount());
            ps.setInt(3, offer.getUser().getId());
            ps.setInt(4, offer.getItem().getId());

            rowsChanged = ps.executeUpdate();

        } catch (SQLException | IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

        return rowsChanged >= 1;

    }

    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM offer WHERE user_id = ?";

        int rowsChanged = -1;
        try (Connection c = ConnectionUtil.getConnectionFromFile()) {
            c.setSchema(schema);
            PreparedStatement ps = c.prepareStatement(sql);

            ps.setInt(1, userId);

            rowsChanged = ps.executeUpdate();

        } catch (SQLException | IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

        return rowsChanged >= 1;
    }

    public boolean delete(int userId, int itemId) {
        String sql = "DELETE FROM offer WHERE user_id = ? AND item_id = ?";

        int rowsChanged = -1;
        try (Connection c = ConnectionUtil.getConnectionFromFile()) {
            c.setSchema(schema);
            PreparedStatement ps = c.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setInt(2, itemId);

            rowsChanged = ps.executeUpdate();

        } catch (SQLException | IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

        return rowsChanged >= 1;
    }
}
