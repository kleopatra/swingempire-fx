/*
 * Created on 21.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;



/**
 * @author dosiennik
 */
public class Person22463 {

        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Person22463 other = (Person22463) obj;
            if (this.id != other.id) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (int) (this.id ^ (this.id >>> 32));
            return hash;
        }

        @Override
        public String toString() {
            return name + id;
        }
        
        
}