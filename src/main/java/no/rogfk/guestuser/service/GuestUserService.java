package no.rogfk.guestuser.service;

import no.rogfk.guestuser.model.GuestUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;

import javax.naming.directory.SearchControls;
import java.util.List;

@Component
public class GuestUserService {

    @Autowired
    ConfigService configService;
    @Autowired
    GuestUserObjectService guestUserObjectService;
    @Autowired
    private LdapTemplate ldapTemplate;
    private SearchControls searchControls;


    public GuestUserService() {

        searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    public boolean create(GuestUser guestUser) {
        guestUserObjectService.setupTodaysGuestUser(guestUser);
        if (!exists(guestUser.getDn())) {
            ldapTemplate.create(guestUser);
            return true;
        }
        return false;
    }


    public boolean update(GuestUser guestUser) {
        if (exists(guestUser.getDn())) {
            ldapTemplate.update(guestUser);
            return true;
        }
        return false;
    }

    public void historizeGuestUser(GuestUser guestUser) {
        // TODO: 22.01.2017  Move user to historical ou:
        //  1. Make sure we got the latest version of the guest
        //  2. Create user in historical ou
        //  3. Delete user in todays ou

        GuestUser histGuestUser = GuestUser.newInstance(guestUser);

        guestUserObjectService.setupHistoricalGuestUser(histGuestUser);

        ldapTemplate.create(histGuestUser);
        ldapTemplate.delete(guestUser);
    }

    public List<GuestUser> getTodaysGuests() {
        return ldapTemplate.findAll(configService.getTodaysGuestBase(), searchControls, GuestUser.class);
    }

    public List<GuestUser> getHistoricalGuests() {
        return ldapTemplate.findAll(configService.getHistGuestBase(), searchControls, GuestUser.class);
    }

    public List<GuestUser> getAllGuests() {
        return ldapTemplate.findAll(configService.getGuestBase(), searchControls, GuestUser.class);
    }

    public boolean exists(String dn) {
        try {
            ldapTemplate.lookup(LdapNameBuilder.newInstance(dn).build());
            return true;
        } catch (org.springframework.ldap.NamingException e) {
            return false;
        }
    }
}