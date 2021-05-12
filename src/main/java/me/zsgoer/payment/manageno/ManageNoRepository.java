package me.zsgoer.payment.manageno;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ManageNoRepository extends JpaRepository<ManageNo,Long> {
    ManageNo findByManageNo(String manageNo);
}
